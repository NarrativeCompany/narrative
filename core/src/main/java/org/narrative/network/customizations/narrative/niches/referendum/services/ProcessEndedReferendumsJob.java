package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.QuartzUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.services.ChunkedDataNicheCustomizationAreaJobBase;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;

import java.math.BigDecimal;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/13/18
 * Time: 4:43 PM
 */
@DisallowConcurrentExecution
public class ProcessEndedReferendumsJob extends ChunkedDataNicheCustomizationAreaJobBase {
    private static final NetworkLogger logger = new NetworkLogger(ProcessEndedReferendumsJob.class);

    private static final int MINUTES_BETWEEN_RUNS = 1;

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected String getLoggingEntityName() {
        return "Ended Referendums";
    }

    @Override
    protected List<OID> getData() {
        return Referendum.dao().getExpiredReferendumOids();
    }

    @Override
    protected void processChunk(List<OID> referendumOidChunk) {
        for (OID referendumOid : referendumOidChunk) {
            // jw: fetch and validate referendum
            Referendum referendum = Referendum.dao().get(referendumOid);
            assert exists(referendum) : "We should always have the referendum since its OID came from HQL!";
            if (!referendum.isOpen()) {
                continue;
            }

            // jw: convert the vote counts into BigDecimal for super accurate testing!
            BigDecimal totalVotes = BigDecimal.valueOf(referendum.getTotalVotes());
            BigDecimal totalVotePoints = BigDecimal.valueOf(referendum.getTotalVotePoints());
            BigDecimal totalVotePointsFor = BigDecimal.valueOf(referendum.getVotePointsFor());

            // jw: Okay, let's process this bad boy!
            processReferendum(referendum, totalVotes, totalVotePoints, totalVotePointsFor);
        }
    }

    private void processReferendum(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints, BigDecimal totalVotePointsFor) {
        // jw: if the referendum was already processed, short out!
        if (!referendum.isOpen()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping referendum " + referendum.getOid() + " because it was already closed before we got to it! How/Why?");
            }
            return;
        }

        ReferendumType type = referendum.getType();

        if (!type.doesReferendumHaveEnoughVotes(referendum, totalVotes, totalVotePoints)) {
            areaContext().doAreaTask(new RescheduleReferendumTask(referendum));
            return;
        }

        // jw: since we didn't reschedule, the referendum is now ended
        areaContext().doAreaTask(new EndReferendumTask(referendum, totalVotePoints, totalVotePointsFor));
    }

    public static void registerForArea(Area area) {
        registerForArea(ProcessEndedReferendumsJob.class, area, QuartzUtil.makeMinutelyTrigger(MINUTES_BETWEEN_RUNS), true);
    }
}
