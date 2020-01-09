package org.narrative.network.core.watchlist.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.content.base.Content;
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
 * User: paul
 * Date: May 2, 2006
 * Time: 9:45:48 AM
 */
public class InstantWatchedContentEmailJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(InstantWatchedContentEmailJob.class);

    private static final String CONTENT_OID = "contentOid";
    private static final String REPLY_OID = "replyOid";
    private static final String IS_EDIT = "isEdit";

    @Deprecated //Quartz Only
    public InstantWatchedContentEmailJob() {
        super(false);
    }

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        long startTime = System.currentTimeMillis();

        final OID replyOid = getOidFromContext(context, REPLY_OID);
        final OID contentOid = getOidFromContext(context, CONTENT_OID);
        final boolean isEdit = context.getMergedJobDataMap().getBoolean(IS_EDIT);

        //get the content and do a composition task
        final Content content = Content.dao().get(contentOid);

        if (!exists(content)) {
            return;
        }

        final SendCompositionConsumerInstantNotificationTaskBase task;
        if (replyOid == null) {
            assert !isEdit : "Notifications of edits not supported!";

            task = new SendContentInstantNotificationTask(content, isEdit);
        } else {
            assert !isEdit : "Don't support edited comment/reply notifications anymore.";

            task = new SendReplyInstantNotificationTask(content, replyOid);
        }

        // bl: going to process these in the area/realm for the content now in order for sandboxing
        getNetworkContext().doCompositionTask(content.getCompositionPartition(), task);

        long timeSinceStart = System.currentTimeMillis() - startTime;
        if (timeSinceStart > IPDateUtil.MINUTE_IN_MS * 5) {
            logger.warn("Failed running InstantEmailTaskHandler in a timely fashion!", contentOid, replyOid, isEdit);
        }
    }

    public static void schedule(Timestamp sendTime, Content content, boolean isEdit, OID replyOid) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(InstantWatchedContentEmailJob.class);

        addAreaToJobDataMap(content.getArea(), jobBuilder);
        jobBuilder.usingJobData(CONTENT_OID, content.getOid().getValue());
        jobBuilder.usingJobData(IS_EDIT, isEdit);

        final String jobName;
        if (replyOid != null) {
            jobBuilder.usingJobData(REPLY_OID, replyOid.getValue());
            jobName = "/c/" + content.getOid() + "/r/" + replyOid;
        } else {
            jobName = "/c/" + content.getOid();
        }

        if (sendTime == null) {
            QuartzJobScheduler.GLOBAL.schedule(jobName, jobBuilder);
        } else {
            QuartzJobScheduler.GLOBAL.schedule(jobName, jobBuilder, sendTime);
        }
    }
}
