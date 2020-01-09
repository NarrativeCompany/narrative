package org.narrative.network.customizations.narrative.payments.services;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.neo.services.NeoscanTransactionMetadata;
import org.narrative.network.customizations.narrative.neo.services.NeoscanUtils;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.hibernate.LockMode;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 10/4/19
 * Time: 9:23 AM
 *
 * @author brian
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class DetectInvoicePaymentsOnNeoscanBaseJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(DetectInvoicePaymentsOnNeoscanBaseJob.class);

    /**
     * bl: we've shut off neo-python payment detection at this point, so let's just look for anything 30 seconds old
     */
    private static final long NEO_PYTHON_PAYMENT_BUFFER_SECONDS = 30;

    private static final String TRANSACTION_AUDIT_COMPLETE_THROUGH_DATE = "TRANSACTION_AUDIT_COMPLETE_THROUGH_DATE";

    private String paymentNeoAddress;
    private String nrveScriptHash;

    private int page = 1;

    // all of these long values are in seconds since that's what Neoscan's API uses. we'll normalize any ms values
    // into second values below.
    private Instant newestTransactionTime;
    private Instant auditTransactionsThroughDate;
    private Instant auditCompleteThroughDate;

    protected abstract String getPaymentNeoAddress();
    protected abstract String getEmailNotificationBody(NrvePayment payment);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        Area narrativeArea = Area.dao().getNarrativePlatformArea();
        getNetworkContext().doAreaTask(narrativeArea, new AreaTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
                // bl: only audit transactions up until 30 seconds ago, but nothing newer. that will give
                // neo-python up to 30 seconds to process the transaction.
                auditTransactionsThroughDate = Instant.now().minus(NEO_PYTHON_PAYMENT_BUFFER_SECONDS, ChronoUnit.SECONDS);
                // bl: the first time we run, let's assume we processed everything up to 3 days ago
                if(!jobDataMap.containsKey(TRANSACTION_AUDIT_COMPLETE_THROUGH_DATE)) {
                    auditCompleteThroughDate = Instant.now().minus(3, ChronoUnit.DAYS);
                    // let's also set the newestTransactionTime to the same timestamp so that we'll store
                    // it at a minimum going forward
                    newestTransactionTime = auditCompleteThroughDate;
                } else {
                    // bl: the setting is currently stored in an epoch second value, so convert it here
                    auditCompleteThroughDate = Instant.ofEpochSecond(jobDataMap.getLong(TRANSACTION_AUDIT_COMPLETE_THROUGH_DATE));
                }
                paymentNeoAddress = getPaymentNeoAddress();
                // if there isn't a NEO address, then bail out and don't hit neoscan's servers with null values unnecessarily.
                if(isEmpty(paymentNeoAddress)) {
                    return null;
                }
                SandboxedCommunitySettings settings = getAreaContext().getAuthZone().getSandboxedCommunitySettings();
                nrveScriptHash = settings.getNrveScriptHash();

                while(true) {
                    try {
                        if(!processPage()) {
                            break;
                        }
                    } catch(Exception e) {
                        // if there is a failure, reset newestTransactionTime so that we'll try again next time
                        newestTransactionTime = null;
                        // bl: if any other exception happens, let's bail out and log the issue
                        StatisticManager.recordException(e, false, null);
                        logger.error("Failed processing Neoscan API data for URL/" + getCurrentUrlForDebug(), e);
                        break;
                    }
                    page++;
                }

                // if we found a newer transaction, record it in the job details.
                // note that we're intentionally only storing dates based on actual transactions.
                // we can't assume neoscan will be up to date to the minute (and in fact, it often lags behind)
                // so we'll only update based on data actually fed to us.
                if(newestTransactionTime!=null) {
                    // bl: existing settings were in epoch seconds, so keeping that legacy support around
                    jobDataMap.put(TRANSACTION_AUDIT_COMPLETE_THROUGH_DATE, newestTransactionTime.getEpochSecond());
                }

                return null;
            }
        });
    }

    private String getCurrentUrlForDebug() {
        return NeoscanUtils.getNeoscanApiBaseUrl() + NeoscanUtils.NEOSCAN_ADDRESS_TRANSACTIONS_PATH + paymentNeoAddress + "/" + page;
    }

    private boolean processPage() {
        try {
            List<NeoscanTransactionMetadata> transactions = NeoscanUtils.getTransactionsPage(paymentNeoAddress, page);
            if(isEmptyOrNull(transactions)) {
                return false;
            }
            for (NeoscanTransactionMetadata transaction : transactions) {
                Instant txTime = transaction.getTransactionDatetime();
                // skip any transactions newer than the date through which we are auditing
                if(txTime.isAfter(auditTransactionsThroughDate)) {
                    continue;
                }
                // once we find a transaction older than our complete through date, we're done looking
                if(!auditCompleteThroughDate.isBefore(txTime)) {
                    return false;
                }
                // this is a new transaction, so record the newest transaction time
                if(newestTransactionTime==null || txTime.isAfter(newestTransactionTime)) {
                    newestTransactionTime = txTime;
                }
                // now, let's check the asset to see if it's NRVE. skip it if not!
                if(!IPStringUtil.isStringEqualIgnoreCase(nrveScriptHash, transaction.getAsset())) {
                    continue;
                }
                // check if it's a transfer to our payment address
                if(!isEqual(paymentNeoAddress, transaction.getAddressTo())) {
                    continue;
                }
                // at this point, it's a candidate for payment! let's get the amount and the
                // sender address so we can look it up!
                String fromAddress = transaction.getAddressFrom();
                NrveValue nrveValue = new NrveValue(transaction.getAmount());
                String txId = transaction.getTransactionId();
                checkTransaction(fromAddress, nrveValue, txId, txTime);
            }
            return true;
        } catch (IOException e) {
            // if there is a failure, reset newestTransactionTime so that we'll try again next time
            newestTransactionTime = null;
            StatisticManager.recordException(e, false, null);
            logger.error("Failed fetching Neoscan API data for URL/" + getCurrentUrlForDebug(), e);
            return false;
        }
    }

    private void checkTransaction(String fromAddress, NrveValue nrveValue, String txId, Instant txTime) {
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                // select the payment record for update so it gets locked in the database
                NrvePayment payment = NrvePayment.dao().getPendingPayment(fromAddress, nrveValue, LockMode.PESSIMISTIC_WRITE);
                if(!exists(payment)) {
                    // if we couldn't identify an unpaid payment, let's see if we can identify it by transaction ID
                    payment = NrvePayment.dao().getUniqueBy(new NameValuePair<>(NrvePayment.Fields.transactionId, txId));
                    // if we still couldn't find it, then that's a possible error case, so log a warning
                    if(!exists(payment)) {
                        if(logger.isWarnEnabled()) logger.warn("Failed identifying transaction! Possible refund required! from/" + fromAddress + " nrve/" + nrveValue + " tx/" + txId);
                        String emailBody = "Neoscan detected possible refund required. sent from " + fromAddress + ". nrve/" + nrveValue + " tx/" + txId;
                        sendEmail("Unidentified Transaction Detected by Neoscan", emailBody);
                    } else {
                        if(logger.isInfoEnabled()) logger.info("Skipping transaction as already processed! from/" + fromAddress + " nrve/" + nrveValue + " tx/" + txId);
                    }
                    return null;
                }
                if(!isEmpty(payment.getTransactionId())) {
                    if(!isEqual(payment.getTransactionId(), txId)) {
                        if(logger.isWarnEnabled()) logger.warn("Found payment with a different transaction ID processed by neo-python. Skipping! from/" + fromAddress + " nrve/" + nrveValue + " tx/" + txId + " actualTx/" + payment.getTransactionId());
                    } else {
                        if(logger.isInfoEnabled()) logger.info("Found payment, but transaction has already been processed by neo-python. Skipping! from/" + fromAddress + " nrve/" + nrveValue + " tx/" + txId);
                    }
                    return null;
                }
                // if we found the record, then we need to mark it as found by the API!
                // this flag is now purely informational to let us know the origin of the payment detection.
                // we are no longer going to do manual review/confirmation of the transaction before the payment is fully processed.
                payment.setFoundByExternalApi(true);
                // also set the transaction details
                payment.setTransactionId(txId);
                payment.setTransactionDate(new Date(txTime.toEpochMilli()));

                String emailBody = getEmailNotificationBody(payment);
                emailBody += "\n" + NeoscanUtils.getTransactionUrl(txId);
                sendEmail("New Payment Detected on Neoscan", emailBody);

                return null;
            }
        });
    }

    private void sendEmail(String subject, String body) {
        PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> NetworkRegistry.getInstance().sendDevOpsStatusEmail(subject, body));
    }
}
