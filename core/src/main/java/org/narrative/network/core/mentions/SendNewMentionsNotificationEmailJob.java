package org.narrative.network.core.mentions;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/27/16
 * Time: 1:13 PM
 */
public class SendNewMentionsNotificationEmailJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(SendNewMentionsNotificationEmailJob.class);

    private static final String CONSUMER_OID = "consumerOid";
    private static final String CONSUMER_TYPE = "consumerType";
    private static final String REPLY_OID = "replyOid";

    @Deprecated //Quartz Only
    public SendNewMentionsNotificationEmailJob() {
        // jw: note, we want this to be force writeable because if this actually sends anything we will be updating the
        //     the Composition/Reply.
    }

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        long startTime = System.currentTimeMillis();

        final CompositionConsumerType consumerType = getIntegerEnumFromContext(context, CompositionConsumerType.class, CONSUMER_TYPE);
        final OID consumerOid = getOidFromContext(context, CONSUMER_OID);
        final OID replyOid = getOidFromContext(context, REPLY_OID);

        assert consumerType != null : "Should always have a consumerType!";
        assert consumerOid != null : "Should always have a consumerOid!";

        // jw: lets short out if the consumer type does not support mentions. I would assert this but if we ever change one
        //     during a release its possible that a old job could still exist for a type that no longer supports it!
        // jw: Its important to note that if this is for a reply it does not matter if the consumer type supports mentions.
        if (replyOid == null && !consumerType.isSupportsMentions()) {
            return;
        }

        //get the content and do a composition task
        final CompositionConsumer consumer = (CompositionConsumer) consumerType.getCompositionType().getDAO().get(consumerOid);

        // jw: if the consumer no longer exists or its not live then bail!
        if (!exists(consumer) || !consumer.isLive()) {
            return;
        }

        if (!consumer.isLive()) {
            return;
        }

        // jw: Now that we have everything lets go ahead and process this!
        getNetworkContext().doCompositionTask(consumer.getCompositionPartition(), new SendNewMentionsInstantNotificationTask(consumer, replyOid));

        long timeSinceStart = System.currentTimeMillis() - startTime;
        if (timeSinceStart > IPDateUtil.MINUTE_IN_MS * 5) {
            logger.warn("Failed sending Mentions Notifications in a timely fashion!", consumerType, consumerOid, replyOid);
        }
    }

    public static void schedule(Timestamp sendTime, CompositionConsumer consumer, OID replyOid) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(SendNewMentionsNotificationEmailJob.class);

        addAreaToJobDataMap(consumer.getArea(), jobBuilder);
        jobBuilder.usingJobData(CONSUMER_TYPE, consumer.getCompositionConsumerType().getId());
        jobBuilder.usingJobData(CONSUMER_OID, consumer.getOid().getValue());

        final String jobName;
        if (replyOid != null) {
            jobBuilder.usingJobData(REPLY_OID, replyOid.getValue());
            jobName = "/c/" + consumer.getOid() + "/r/" + replyOid;
        } else {
            jobName = "/c/" + consumer.getOid();
        }

        if (sendTime == null) {
            QuartzJobScheduler.GLOBAL.schedule(jobName, jobBuilder);
        } else {
            QuartzJobScheduler.GLOBAL.schedule(jobName, jobBuilder, sendTime);
        }
    }
}
