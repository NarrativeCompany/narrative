package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationExpiringReminderType;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.sql.Timestamp;
import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Date: 2019-08-09
 * Time: 11:30
 *
 * @author jonmark
 */
public class ProcessPublicationExpiringJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessPublicationExpiringJob.class);

    private static final String PUBLICATION_OID = "publicationOid";
    private static final String REMINDER_TYPE = "reminderType";

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        OID publicationOid = getOidFromContext(context, PUBLICATION_OID);
        PublicationExpiringReminderType reminderType = getIntegerEnumFromContext(context, PublicationExpiringReminderType.class, REMINDER_TYPE);
        assert publicationOid != null : "We should always find an OID here, if we didn't then there is something really weird going on.";
        assert reminderType != null : "We should always find an ReminderType here, if we didn't then there is something really weird going on.";

        Publication publication = Publication.dao().get(publicationOid);
        if (!exists(publication)) {
            return;
        }

        // jw: to allow each reminderType to use different handlers for notifying the admin I am setting this up to have
        //     the type create the processor.
        getAreaContext().doAreaTask(new SendPublicationExpiringReminderEmail(publication, reminderType));

        if (logger.isDebugEnabled()) {
            logger.debug("Executed reminder for publication/" + publication.getOid());
        }
    }

    private static String getTriggerName(Publication publication, PublicationExpiringReminderType reminderType) {
        return "ProcessPublicationExpiringJob/" + publication.getOid()+"/"+reminderType.getId();
    }

    public static void unschedule(Publication publication) {
        for (PublicationExpiringReminderType reminderType : PublicationExpiringReminderType.values()) {
            String jobName = getTriggerName(publication, reminderType);
            QuartzJobScheduler.GLOBAL.removeTrigger(jobName, null);
        }
    }

    public static void schedule(Publication publication) {
        assert exists(publication) : "A publication should always be provided!";
        assert publication.getStatusResolved().isActive() : "The provided publication should always be active at the time of scheduling.";

        // jw: let's go ahead and schedule all reminder emails immediately.
        for (PublicationExpiringReminderType reminderType : PublicationExpiringReminderType.values()) {
            Instant sendAt = publication.getEndDatetime();
            if (reminderType.getSendBeforeEndDatetime()!=null) {
                sendAt = sendAt.minus(reminderType.getSendBeforeEndDatetime());
            }

            // jw: if the time has already passed for this reminder email, then let's go ahead and skip it.
            if (sendAt.isBefore(Instant.now())) {
                continue;
            }

            String name = getTriggerName(publication, reminderType);

            Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(name, null);

            assert trigger == null : "ProcessPublicationExpiringJob should only be scheduled once per publication";

            TriggerBuilder triggerBuilder = newTrigger()
                    .withIdentity(name)
                    .forJob(ProcessPublicationExpiringJob.class.getSimpleName())
                    .startAt(new Timestamp(sendAt.toEpochMilli()))
                    .usingJobData(PUBLICATION_OID, publication.getOid().getValue())
                    .usingJobData(REMINDER_TYPE, reminderType.getId())
                    ;
            addAreaToJobDataMap(Area.dao().getNarrativePlatformArea(), triggerBuilder);

            QuartzJobScheduler.GLOBAL.schedule(triggerBuilder);
        }
    }
}
