package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/28/18
 * Time: 1:48 PM
 * <p>
 * result indicates whether the invoice will need to be refreshed. EG, if it was changed in a separate writeable transaction.
 */
public class EnsureInvoiceFiatPaymentTask extends AreaTaskImpl<Boolean> {
    private final Invoice invoice;

    public EnsureInvoiceFiatPaymentTask(Invoice invoice) {
        // jw: while this task will update the invoice, it is meant to manually upgrade into a writeable task if necessary
        super(false);

        assert !invoice.getType().isImmediateFiatPaymentType() : "This task should never be used for immediate fiat payment types";

        this.invoice = invoice;
    }

    @Override
    protected Boolean doMonitoredTask() {
        // jw: Allow the invoice to determine if fiat payments can be used to fulfill it. This will come in handy down the
        //     road when we might have Invoice types that only support NRVE payments.
        if (!invoice.isSupportsFiatPayment()) {
            return false;
        }

        // jw: if the invoice is not "invoiced", then we can short out.
        if (!invoice.getStatus().isInvoiced()) {
            return false;
        }

        // jw: if we are in a writeable transaction we can just update this inline
        if (!PartitionType.GLOBAL.currentSession().isReadOnly()) {
            setupInvoice(invoice);
            // jw: since this change happened in the same session there is no need to reload the object
            return false;
        }

        // jw: since we are in a read only transaction, we need to start a new one in a writeable state.
        return TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Boolean>() {
            @Override
            protected Boolean doMonitoredTask() {
                // jw: since we are in a new transaction we need to get a new object
                Invoice invoiceToUpdate = Invoice.dao().get(invoice.getOid());

                return setupInvoice(invoiceToUpdate);
            }
        });
    }

    private boolean setupInvoice(Invoice invoice) {
        // jw: before we go to update this, let's lock the auction. This is the object we lock on everywhere else, so
        //     let's make sure to use the same point of intersection.
        invoice.lockForProcessing();

        // jw: now that we have the lock, let's refresh the invoice to ensure that if someone else beat us, we do not
        //     duplicate their effort.
        Invoice.dao().refresh(invoice);

        // jw: now that all of that is behind us, we can go ahead and see what we need to do to get the fiat payment
        //     setup and ready to go.
        FiatPayment fiatPayment = invoice.getFiatPayment();
        // jw: first, do we need to create a payment?
        if (!exists(fiatPayment)) {
            // jw: if we have to generate a FiatPayment then use the invoiceConsumer to get the NrveUsdPrice
            FiatPayment.dao().save(new FiatPayment(invoice));
            return true;
        }

        // bl: if the payment already exists, then we're done! we no longer recalculate USD amounts now that we are fixing
        // the price of NRVE for auctions.
        return false;
    }
}
