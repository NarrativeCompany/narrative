package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.services.ProcessExpiredInvoiceTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 7:47 AM
 */
@DisallowConcurrentExecution
public class ProcessExpiredInvoicesJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessExpiredInvoicesJob.class);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        List<OID> invoiceOids = Invoice.dao().getExpiredInvoicedOids();

        if (invoiceOids.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No expired invoices to process. Shorting out!");
            }
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Processing "+invoiceOids.size()+" expired invoices.");
        }

        int processed = 0;
        for (OID invoiceOid : invoiceOids) {
            // jw: this task should not happen often, so let's go ahead and log out each time.
            if (logger.isInfoEnabled()) {
                logger.info("Processing expired invoice/"+invoiceOid);
            }
            // jw: let's process each invoice atomically.
            TaskRunner.doRootAreaTask(Area.dao().getNarrativePlatformArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    getAreaContext().doAreaTask(new ProcessExpiredInvoiceTask(Invoice.dao().get(invoiceOid)));
                    return null;
                }
            });
            processed++;
            // jw: log out every 10
            if (processed % 10 == 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Processed "+processed+" out of "+invoiceOids.size()+" expired invoices.");
                }
            }
        }
    }
}
