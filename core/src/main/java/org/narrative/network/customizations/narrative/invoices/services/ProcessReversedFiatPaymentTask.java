package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.FiatPaymentStatus;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.SendPaymentChargebackEmail;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-25
 * Time: 15:02
 *
 * @author jonmark
 */
public class ProcessReversedFiatPaymentTask extends AreaTaskImpl<FiatPayment> {
    private static final NetworkLogger logger = new NetworkLogger(ProcessReversedFiatPaymentTask.class);

    private final FiatPaymentProcessorType processorType;
    private final String transactionId;
    private final boolean forChargeback;

    public ProcessReversedFiatPaymentTask(FiatPaymentProcessorType processorType, String transactionId, boolean forChargeback) {
        this.processorType = processorType;
        this.transactionId = transactionId;
        this.forChargeback = forChargeback;
    }

    @Override
    protected FiatPayment doMonitoredTask() {
        FiatPayment payment = FiatPayment.dao().getByTransactionId(transactionId);
        // jw: If we don't find a payment let's short out since there is nothing to do on our side.
        if (!exists(payment)) {
            String message = "No payment found for processor/"+processorType+" by transactionId/" + transactionId;
            UnexpectedError error = UnexpectedError.getRuntimeException(message);
            StatisticManager.recordException(error, false, null);
            logger.error(message, error);

            return null;
        }

        Invoice invoice = payment.getInvoice();

        InvoiceStatus originalInvoiceStatus = invoice.getStatus();

        // jw: Let's assume that if the status is already for a reversed payment, we do not need to process this again.
        if (originalInvoiceStatus.isReversedPayment()) {
            return payment;

        // otherwise if the invoice has never been paid, we should fail since that should really never happen!
        } else if (!originalInvoiceStatus.isHasBeenPaid()) {
            throw UnexpectedError.getRuntimeException("Attempting to reverse the payment on a non-paid invoice/"+invoice+" origStatus/"+originalInvoiceStatus + " forChargeback/" + forChargeback);
        }

        // jw: Let's log out the details for this, just in case we need to track anything down.
        if (logger.isInfoEnabled()) {
            logger.info("Invoice/" + invoice.getOid() + " was disputed for transactionId/" + transactionId + " so we are revoking payment/" + payment.getOid());
        }

        payment.setStatus(FiatPaymentStatus.getReversedPaymentStatus(forChargeback));
        invoice.updateStatus(InvoiceStatus.getReversedPaymentStatus(forChargeback));
        Instant updateDatetime = Instant.ofEpochMilli(invoice.getUpdateDatetime().getTime());

        if (forChargeback) {
            AreaUserRlm areaUserRlm = AreaUser.getAreaUserRlm(invoice.getUser().getLoneAreaUser());

            // jw: this ledger entry will end up sending the conduct event to the reputation engine.
            LedgerEntry ledgerEntry = new LedgerEntry(areaUserRlm, LedgerEntryType.PAYMENT_CHARGEBACK);
            invoice.getType().addLedgerEntryFields(ledgerEntry, invoice);

            // jw: if this is the first time that the bidder has received a chargeback, we need to let them know that
            //     their ability to bid is being revoked.
            if (!invoice.getUser().isHasReceivedPaymentChargeback()) {
                getAreaContext().doAreaTask(new SendPaymentChargebackEmail(payment));
            }

            // jw: now, let's update the last chargeback time.
            invoice.getUser().setLastPaymentChargebackDatetime(updateDatetime);
        }

        AreaTaskImpl<Object> reversedPaymentHandler = invoice.getType().getReversedFiatPaymentHandler(payment, forChargeback, originalInvoiceStatus);

        // jw: not all invoice types need to do anything fancy or special when a charge gets reversed.
        if (reversedPaymentHandler!=null) {
            getAreaContext().doAreaTask(reversedPaymentHandler);
        }

        return payment;
    }
}
