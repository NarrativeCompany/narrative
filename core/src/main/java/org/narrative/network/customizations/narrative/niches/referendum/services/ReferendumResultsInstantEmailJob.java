package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
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
 * Date: 4/3/18
 * Time: 9:52 AM
 */
public class ReferendumResultsInstantEmailJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ReferendumResultsInstantEmailJob.class);
    private static final String REFERENDUM_OID = "referendumOid";

    public ReferendumResultsInstantEmailJob() {
        super(true);
    }

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        OID referendumOid = getOidFromContext(context, REFERENDUM_OID);

        Referendum referendum = Referendum.dao().get(referendumOid);

        if (!exists(referendum)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No referendum found for results instant email job:" + referendumOid);
            }
            return;
        }

        // jw: first, send the results emails
        getAreaContext().doAreaTask(new SendReferendumResultsEmail(referendum));
    }

    public static void schedule(Referendum referendum) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(ReferendumResultsInstantEmailJob.class).usingJobData(REFERENDUM_OID, referendum.getOid().getValue());
        addAreaToJobDataMap(referendum.getArea(), jobBuilder);

        QuartzJobScheduler.GLOBAL.schedule("referendumResults/r/" + referendum.getOid(), jobBuilder);
    }
}
