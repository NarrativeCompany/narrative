package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/2/18
 * Time: 2:51 PM
 */
public class ReferendumCommentInstantEmailJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ReferendumCommentInstantEmailJob.class);
    private static final String REFERENDUM_OID = "referendumOid";
    private static final String REPLY_OID = "replyOid";

    public ReferendumCommentInstantEmailJob() {
        super(false);
    }

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        //get the Referendum and do a composition task
        OID referendumOid = getOidFromContext(context, REFERENDUM_OID);

        Referendum referendum = Referendum.dao().get(referendumOid);

        if (!exists(referendum)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No referendum found for comment instant email job:" + referendumOid);
            }
            return;
        }


        OID replyOid = getOidFromContext(context, REPLY_OID);

        getNetworkContext().doCompositionTask(referendum.getCompositionPartition(), new SendReferendumCommentInstantNotificationTask(referendum, replyOid));
    }

    public static void schedule(Referendum referendum, Reply reply) {
        // jw: we only want to send these emails for tribunal referendums now!
        if (!referendum.getType().isTribunalReferendum()) {
            return;
        }

        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(ReferendumCommentInstantEmailJob.class).usingJobData(REFERENDUM_OID, referendum.getOid().getValue()).usingJobData(REPLY_OID, reply.getOid().getValue());
        addAreaToJobDataMap(referendum.getArea(), jobBuilder);

        QuartzJobScheduler.GLOBAL.schedule("/c/" + referendum.getOid() + "r/" + reply.getOid(), jobBuilder);
    }
}
