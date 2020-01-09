package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.watchlist.services.InstantWatchedContentEmailJob;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 2, 2006
 * Time: 9:06:03 AM
 */
public class NewReplyAreaJob extends AreaJob {
    private final static String CONTENT_OID = "contentOid";
    private final static String REPLY_OID = "replyOid";
    private final static String IS_EDIT = "isEdit";

    @Deprecated // Quartz Only
    public NewReplyAreaJob() {}

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        final OID contentOid = getOidFromContext(context, CONTENT_OID);
        final OID replyOid = getOidFromContext(context, REPLY_OID);
        final boolean isEdit = context.getMergedJobDataMap().getBoolean(IS_EDIT);

        Content content = Content.dao().get(contentOid);
        if (exists(content)) {
            // bl: no longer support edited comment/reply notifications
            if (!isEdit) {
                //create a new reply scheduled task
                InstantWatchedContentEmailJob.schedule(null, content, false, replyOid);
            }
        }
    }

    public static void schedule(Content content, Reply reply, boolean isEdit) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(NewReplyAreaJob.class);
        jobBuilder.usingJobData(CONTENT_OID, content.getOid().getValue());
        jobBuilder.usingJobData(REPLY_OID, reply.getOid().getValue());
        jobBuilder.usingJobData(IS_EDIT, isEdit);
        QuartzJobScheduler.GLOBAL.scheduleAreaJob(content.getArea(), "/c/" + content.getOid() + "/r/" + reply.getOid(), jobBuilder);
    }
}
