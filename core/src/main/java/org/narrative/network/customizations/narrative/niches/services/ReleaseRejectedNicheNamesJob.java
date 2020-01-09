package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.network.core.quartz.NarrativeAreaJob;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Date: 2019-04-19
 * Time: 12:59
 *
 * @author brian
 */
@DisallowConcurrentExecution
public class ReleaseRejectedNicheNamesJob extends NarrativeAreaJob {
    /**
     * release niche names after 30 days
     */
    private static final int RELEASE_NICHE_NAMES_AFTER_DAYS = 30;
    @Override
    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        Instant releaseBefore = Instant.now().minus(RELEASE_NICHE_NAMES_AFTER_DAYS, ChronoUnit.DAYS);
        List<Niche> nichesToRelease = Niche.dao().getReservedRejectedNichesOlderThan(getAreaContext().getPortfolio(), releaseBefore);
        for (Niche niche : nichesToRelease) {
            // release it!
            niche.releaseReservedNameForPermanentlyRejectedNiche();
        }
    }
}
