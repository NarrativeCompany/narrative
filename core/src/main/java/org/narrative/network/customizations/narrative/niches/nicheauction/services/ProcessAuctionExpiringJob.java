package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
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
public class ProcessAuctionExpiringJob extends AreaJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessAuctionExpiringJob.class);

    private static final String JOB_GROUP = "NICHE_AUCTION_EXPIRING";

    private static final String NICHE_AUCTION_OID = "nicheAuctionOid";

    private static final long REMINDER_BEFORE_AUCTION_END_AT = IPDateUtil.HOUR_IN_MS;

    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        NicheAuction auction = cast(NicheAuction.dao().get(getOidFromContext(context, NICHE_AUCTION_OID)), NicheAuction.class);
        // jw: ensure we have an auction, and that it's still open for bidding.
        if (!exists(auction) || !auction.isOpenForBidding()) {
            return;
        }

        getAreaContext().doAreaTask(new SendAuctionExpiringEmail(auction));

        if (logger.isDebugEnabled()) {
            logger.debug("Executed reminder for auction/" + auction.getOid());
        }
    }

    private static String getJobName(NicheAuction auction) {
        return "ProcessAuctionExpiringJob/" + auction.getOid();
    }

    public static void unschedule(NicheAuction auction) {
        QuartzJobScheduler.GLOBAL.removeTrigger(getJobName(auction), JOB_GROUP);
    }

    public static void schedule(NicheAuction auction) {
        assert exists(auction) : "An auction should always be provided!";
        assert auction.getEndDatetime() != null : "An endDatetime should always be set on the auction/" + auction.getOid();

        //mk: no need to schedule reminder if ending soon. Should never happen though, since job is scheduled upon placing first bid.
        if (auction.getEndDatetime().getTime() <= System.currentTimeMillis() + REMINDER_BEFORE_AUCTION_END_AT) {
            return;
        }

        String name = getJobName(auction);

        Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(name, JOB_GROUP);

        assert trigger == null : "ProcessAuctionExpiringJob should only be scheduled once per auction";

        if (trigger == null) {
            JobBuilder jobBuilder = QuartzJobScheduler.GLOBAL.createRecoverableJobBuilder(ProcessAuctionExpiringJob.class, getJobName(auction));

            TriggerBuilder triggerBuilder = newTrigger().withIdentity(name, JOB_GROUP).startAt(new Timestamp(auction.getEndDatetime().getTime() - REMINDER_BEFORE_AUCTION_END_AT)).forJob(jobBuilder.build()).usingJobData(NICHE_AUCTION_OID, auction.getOid().getValue());
            addAreaToJobDataMap(auction.getNiche().getArea(), triggerBuilder);

            QuartzJobScheduler.GLOBAL.schedule(jobBuilder, triggerBuilder);
        }
    }
}
