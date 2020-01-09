package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/3/18
 * Time: 9:04 AM
 */
public class ReferendumVoteInstantEmailJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ReferendumVoteInstantEmailJob.class);
    private static final String VOTE_OID = "voteOid";

    public ReferendumVoteInstantEmailJob() {
        super(false);
    }

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        OID voteOid = getOidFromContext(context, VOTE_OID);

        ReferendumVote vote = ReferendumVote.dao().get(voteOid);

        if (!exists(vote)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No vote found for referendum vote instant email job:" + voteOid);
            }
            return;
        }

        getAreaContext().doAreaTask(new SendReferendumVoteEmail(vote));
    }

    public static void schedule(ReferendumVote vote) {
        Referendum referendum = vote.getReferendum();

        // jw: we only want to send these emails for tribunal referendums now!
        if (!referendum.getType().isTribunalReferendum()) {
            return;
        }

        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(ReferendumVoteInstantEmailJob.class).usingJobData(VOTE_OID, vote.getOid().getValue());
        addAreaToJobDataMap(referendum.getArea(), jobBuilder);

        QuartzJobScheduler.GLOBAL.schedule("newReferendumVote/v/" + vote.getOid(), jobBuilder);
    }
}
