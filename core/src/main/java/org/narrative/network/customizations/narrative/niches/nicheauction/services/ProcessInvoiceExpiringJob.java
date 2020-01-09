package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 3/30/18
 * Time: 7:38 AM
 */
public class ProcessInvoiceExpiringJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessInvoiceExpiringJob.class);

    private static final String JOB_GROUP = "NICHE_INVOICE_EXPIRING";

    private static final String INVOICE_OID = "invoiceOid";

    private static final long REMINDER_BEFORE_INVOICE_EXPIRE_AT = IPDateUtil.DAY_IN_MS * 3;

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        OID invoiceOid = getOidFromContext(context, INVOICE_OID);
        assert invoiceOid != null : "We should always find an OID here, if we didn't then there is something really weird going on.";

        Invoice invoice = cast(Invoice.dao().get(invoiceOid), Invoice.class);
        if (!exists(invoice) || !invoice.getStatus().isInvoiced()) {
            return;
        }

        getAreaContext().doAreaTask(new SendInvoiceExpiringEmail(invoice));

        if (logger.isDebugEnabled()) {
            logger.debug("Executed invoice reminder for invoice/" + invoice.getOid());
        }
    }

    private static String getJobName(Invoice invoice) {
        return "ProcessInvoiceExpiringJob/" + invoice.getOid();
    }

    public static void unschedule(Invoice invoice) {
        String jobName = getJobName(invoice);
        QuartzJobScheduler.GLOBAL.removeJob(ProcessInvoiceExpiringJob.class, jobName);
        QuartzJobScheduler.GLOBAL.removeTrigger(jobName, JOB_GROUP);
    }

    public static void schedule(Invoice invoice) {
        assert exists(invoice) : "An invoice should always be provided!";
        assert invoice.getStatus().isInvoiced() : "Invoice should have invoiced status/" + invoice.getOid();

        //mk: no need to schedule reminder if ending soon. Should never happen though, since job is scheduled upon creating an invoice.
        if (invoice.getPaymentDueDatetime().getTime() <= System.currentTimeMillis() + REMINDER_BEFORE_INVOICE_EXPIRE_AT) {
            return;
        }

        String name = getJobName(invoice);

        Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(name, JOB_GROUP);

        assert trigger == null : "ProcessInvoiceExpiringJob should only be scheduled once per invoice";

        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(ProcessInvoiceExpiringJob.class, name);

        TriggerBuilder triggerBuilder = newTrigger()
                .withIdentity(name, JOB_GROUP)
                .startAt(new Timestamp(invoice.getPaymentDueDatetime().getTime() - REMINDER_BEFORE_INVOICE_EXPIRE_AT))
                .forJob(jobBuilder.build())
                .usingJobData(INVOICE_OID, invoice.getOid().getValue());
        addAreaToJobDataMap(Area.dao().getNarrativePlatformArea(), triggerBuilder);

        QuartzJobScheduler.GLOBAL.schedule(jobBuilder, triggerBuilder);
    }
}
