package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.QuartzUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.invoices.services.ProcessPaidInvoiceTask;
import org.narrative.network.customizations.narrative.niches.services.ChunkedDataNicheCustomizationAreaJobBase;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 6:53 PM
 */
@DisallowConcurrentExecution
public class ProcessPaidInvoicesJob extends ChunkedDataNicheCustomizationAreaJobBase {
    private static final NetworkLogger logger = new NetworkLogger(ProcessPaidInvoicesJob.class);

    private static final int SECONDS_BETWEEN_RUNS = 10;

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected String getLoggingEntityName() {
        return "Paid Invoices";
    }

    @Override
    protected List<OID> getData() {
        return Invoice.dao().getInvoiceOidsWithPendingNrvePayment();
    }

    @Override
    protected boolean processDataIndividually() {
        return true;
    }

    @Override
    protected void processChunk(List<OID> invoiceOids) {
        for (OID invoiceOid : invoiceOids) {
            Invoice invoice = Invoice.dao().get(invoiceOid);

            // jw: lets lock this before we do any processing.
            invoice.lockForProcessing();

            // jw: if we have a fiatPayment associated with this auction, we need to delete it since we are only concerned
            //     with NRVE payments in this process.
            FiatPayment fiatPayment = invoice.getFiatPayment();
            if (exists(fiatPayment)) {
                invoice.setFiatPayment(null);
                FiatPayment.dao().delete(fiatPayment);
            }

            NrvePayment payment = invoice.getFreshNrvePayment();
            // jw: there are several ways that the payment could have been processed while we were getting to
            //     this point, so lets make sure that it's still in need of processing.
            if (!exists(payment) || payment.getPaymentStatus() == null) {
                // jw: just short out in this case!
                return;
            }

            assert payment.hasBeenPaid() : "There is no way that the payment should not be paid from the blockchain at this point!";

            // jw: finally, just process the payment task so that we will assign ownership and send emails!
            areaContext().doAreaTask(new ProcessPaidInvoiceTask(invoice));
        }
    }

    public static void registerForArea(Area area) {
        registerForArea(ProcessPaidInvoicesJob.class, area, QuartzUtil.makeSecondlyTrigger(SECONDS_BETWEEN_RUNS), true);
    }
}
