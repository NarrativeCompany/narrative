package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.services.ProcessInvoicePendingProratedRefundTask;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

/**
 * Date: 2019-05-31
 * Time: 11:10
 *
 * @author brian
 */
public class ProcessProratedRevenueRefundsStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(ProcessProratedRevenueRefundsStepProcessor.class);

    public ProcessProratedRevenueRefundsStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.PROCESS_PRORATED_REVENUE_REFUNDS);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: get all Invoices from past months that are pending a prorated refund
        List<OID> invoiceOidsPendingRefund = Invoice.dao().getInvoiceOidsByStatusBefore(InvoiceStatus.PENDING_PRORATED_REFUND, period.getRewardYearMonth().getUpperBoundForQuery());
        if(logger.isInfoEnabled()) logger.info("Found " + invoiceOidsPendingRefund.size() + " invoices to process prorated refunds for in period/" + period.getPeriod());
        for (OID invoiceOid : invoiceOidsPendingRefund) {
            // bl: for each iteration, let's do an isolated transaction to keep the session scope limited
            TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    Invoice invoice = Invoice.dao().get(invoiceOid);
                    return getAreaContext().doAreaTask(new ProcessInvoicePendingProratedRefundTask(invoice));
                }
            });
        }

        // jw: let's ensure that the original query no longer contains any results since all prorated refunds should have
        //     been processed above.
        invoiceOidsPendingRefund = Invoice.dao().getInvoiceOidsByStatusBefore(InvoiceStatus.PENDING_PRORATED_REFUND, period.getRewardYearMonth().getUpperBoundForQuery());
        if (!invoiceOidsPendingRefund.isEmpty()) {
            throw UnexpectedError.getRuntimeException("We should have processed all pending prorated refund invoices above, what happened?");
        }

        for (ProratedRevenueType revenueType : ProratedRevenueType.ACTIVE_TYPES) {
            ProratedMonthRevenue proratedMonthRevenue = ProratedMonthRevenue.dao().getForYearMonthAndType(period.getPeriod(), revenueType);
            proratedMonthRevenue.recordBulkRefundsNeoTransaction();
        }

        return null;
    }
}
