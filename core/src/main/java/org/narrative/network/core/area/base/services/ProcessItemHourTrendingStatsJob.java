package org.narrative.network.core.area.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 4/29/13
 * Time: 11:31 AM
 * User: jonmark
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ProcessItemHourTrendingStatsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(ProcessItemHourTrendingStatsJob.class);

    @Deprecated // Quartz only
    public ProcessItemHourTrendingStatsJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        processStats();
    }

    public static void processStats() {
        final Map<OID, Map<Long, ItemHourTrendingStats>> statsToSend = newHashMap();
        final List<ItemHourTrendingStats> allStats = new LinkedList<>();
        {
            ItemHourTrendingStats stats;

            logger.info("Flushing Hour Statistics.  Approximate Queue size: " + ItemHourTrendingStatsManager.getQueueSize());

            // First lets flush out the stats queue and group all of the updates for the same content/clip together
            // so that we will only need to do the write once per content/hour combination.
            while ((stats = ItemHourTrendingStatsManager.pollStatQueue()) != null) {
                Map<Long, ItemHourTrendingStats> hourStats = statsToSend.computeIfAbsent(stats.getObjectOid(), k -> new HashMap<>());
                ItemHourTrendingStats statsForHour = hourStats.get(stats.getHoursSinceEpoch());
                if (statsForHour == null) {
                    hourStats.put(stats.getHoursSinceEpoch(), stats);
                    allStats.add(stats);
                } else {
                    statsForHour.setViewPoints(statsForHour.getViewPoints() + stats.getViewPoints());
                    statsForHour.setReplyPoints(statsForHour.getReplyPoints() + stats.getReplyPoints());
                    statsForHour.setLikePoints(statsForHour.getLikePoints() + stats.getLikePoints());
                }
            }
        }

        for (final ItemHourTrendingStats stats : allStats) {
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    // Lets see if we have a statistics object
                    ItemHourTrendingStats dbStats = ItemHourTrendingStats.dao().getLockedForObjectOidAndHoursSinceEpoch(stats.getObjectOid(), stats.getHoursSinceEpoch());

                    // if we dont have one we can just save the one we currently have
                    if (!exists(dbStats)) {
                        ItemHourTrendingStats.dao().save(stats);

                    // looks like we need to update our existing record with the addition stats
                    } else {
                        dbStats.setViewPoints(dbStats.getViewPoints() + stats.getViewPoints());
                        dbStats.setReplyPoints(dbStats.getReplyPoints() + stats.getReplyPoints());
                        dbStats.setLikePoints(dbStats.getLikePoints() + stats.getLikePoints());
                    }

                    return null;
                }
            });
        }
    }
}
