package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/21/18
 * Time: 7:38 AM
 */
public class ProcessAuctionBeginningPaymentPeriodJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessAuctionBeginningPaymentPeriodJob.class);

    private static final String JOB_GROUP = "NICHE_AUCTION_END";

    private static final String NICHE_AUCTION_OID = "nicheAuctionOid";

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        NicheAuction auction = cast(NicheAuction.dao().get(getOidFromContext(context, NICHE_AUCTION_OID)), NicheAuction.class);
        if (!exists(auction)) {
            return;
        }

        Niche niche = auction.getNiche();
        if (!isEqual(auction, niche.getActiveAuction())) {
            // jw: there are a few niches that were rejected while their auction was active, and this job was left behind
            // let's assume that is what this was about.
            if (!niche.getStatus().isRejected()) {
                String message = "Expected the Niche/"+niche.getOid()+" to be rejected for auction/"+auction.getOid()+" how did we get here?";
                logger.error(message);
                StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            }

            return;
        }

        getAreaContext().doAreaTask(new StartAuctionPaymentPeriodTask(auction));

        if (logger.isDebugEnabled()) {
            logger.debug("Invoiced auction/" + auction.getOid() + " to bidder/" + auction.getLeadingBid().getBidder().getOid());
        }
    }

    private static String getJobName(NicheAuction auction) {
        return "processAuctionBeginningPaymentPeriod/" + auction.getOid();
    }

    public static void unschedule(NicheAuction auction) {
        QuartzJobScheduler.GLOBAL.removeTrigger(getJobName(auction), JOB_GROUP);
    }

    public static void schedule(NicheAuction auction) {
        assert exists(auction) : "An auction should always be provided!";
        assert auction.getEndDatetime() != null : "An endDatetime should always be set on the auction/" + auction.getOid();

        String name = getJobName(auction);

        TriggerBuilder triggerBuilder = newTrigger().withIdentity(name, JOB_GROUP).startAt(auction.getEndDatetime()).forJob(ProcessAuctionBeginningPaymentPeriodJob.class.getSimpleName());
        triggerBuilder.usingJobData(NICHE_AUCTION_OID, auction.getOid().getValue());
        addAreaToJobDataMap(auction.getNiche().getArea(), triggerBuilder);

        Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(name, JOB_GROUP);

        if (trigger == null) {
            QuartzJobScheduler.GLOBAL.schedule(triggerBuilder);
        } else {
            QuartzJobScheduler.GLOBAL.reschedule(trigger.getKey(), triggerBuilder);
        }
    }
}
