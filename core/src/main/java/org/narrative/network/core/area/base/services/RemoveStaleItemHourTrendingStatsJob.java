package org.narrative.network.core.area.base.services;

import org.narrative.network.core.area.base.RoleContentPageView;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Date: 4/30/13
 * Time: 11:04 AM
 * User: jonmark
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class RemoveStaleItemHourTrendingStatsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(RemoveStaleItemHourTrendingStatsJob.class);

    @Deprecated
    public RemoveStaleItemHourTrendingStatsJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        // todo:uncomment for #2489. don't want to prune any history for now.
        /*long startTime = System.currentTimeMillis();

        // lets set the cutoff the limit as defined in ItemHourTrendingStats.
        final long cutoffHours = ItemHourTrendingStats.getHoursSinceTheEpoch() - ItemHourTrendingStats.HISTORY_IN_HOURS;

        ItemHourTrendingStats.dao().deleteStaleObjects(cutoffHours);

        if (logger.isInfoEnabled()) {
            logger.info("RemoveStaleItemHourTrendingStatsJob() Finished performing. (took " + (System.currentTimeMillis() - startTime) + "ms)");
        }*/

        // bl: let's also clean out page view records over a day old
        RoleContentPageView.dao().deleteOldPageViews();
    }
}
