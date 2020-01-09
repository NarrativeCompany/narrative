package org.narrative.network.core.area.base.services;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.QuartzUtil;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 4/29/13
 * Time: 9:43 AM
 * User: jonmark
 */
public class ItemHourTrendingStatsManager {
    private static Queue<ItemHourTrendingStats> statQueue = new ConcurrentLinkedQueue<>();

    public static void recordContentView(PrimaryRole currentRole, Content content) {
        assert content.getContentType().isSupportsTrendingStats() : "Should only ever record hour stats for content that supports it!";
        record(currentRole, content.getRealAuthor(), new ItemHourTrendingStats(content), 1, 0, 0);
    }

    public static void recordContentReply(PrimaryRole currentRole, Content content) {
        assert content.getContentType().isSupportsTrendingStats() : "Should only ever record hour stats for content that supports it!";
        record(currentRole, content.getRealAuthor(), new ItemHourTrendingStats(content), 0, 1, 0);
    }

    public static void addContentLikePoints(PrimaryRole currentRole, Content content, int points) {
        assert content.getContentType().isSupportsTrendingStats() : "Should only ever record hour stats for content that supports it!";
        record(currentRole, content.getRealAuthor(), new ItemHourTrendingStats(content), 0, 0, points);
    }

    public static void removeContentLikePoints(PrimaryRole currentRole, Content content, long msOfEvent, int points) {
        assert content.getContentType().isSupportsTrendingStats() : "Should only ever record hour stats for content that supports it!";
        ItemHourTrendingStats stats = new ItemHourTrendingStats(content, msOfEvent);

        // jw: we only want to remove the points if the original event was within the range of time we are looking at.
        long hoursSinceOriginalLike = ItemHourTrendingStats.getHoursSinceTheEpoch() - stats.getHoursSinceEpoch();
        if (hoursSinceOriginalLike <= ItemHourTrendingStats.CUTOFF_IN_HOURS) {
            record(currentRole, content.getRealAuthor(), stats, 0, 0, -points);
        }
    }

    private static void record(PrimaryRole currentRole, User author, final ItemHourTrendingStats stats, long views, long replies, long likePoints) {
        // bl: don't ever record any statistics during imports
        if (NetworkRegistry.getInstance().isImporting()) {
            return;
        }

        // bl: don't ever record trending actions for the author of the content item
        if (currentRole != null && currentRole.isRegisteredUser() && exists(author) && isEqual(author, currentRole.getUser())) {
            return;
        }

        // bl: the view points and reply points are calculated by multiplying the number of views/replies by
        // the role's reputation-adjusted vote points
        if(exists(currentRole)) {
            if(views!=0) {
                stats.setViewPoints(views * currentRole.getReputationAdjustedVotePoints());
            }
            if(replies!=0) {
                stats.setReplyPoints(replies * currentRole.getReputationAdjustedVotePoints());
            }
        }
        stats.setLikePoints(likePoints);

        PartitionGroup.addEndOfPartitionGroupRunnable(() -> statQueue.add(stats));
    }

    private static boolean init = false;

    public static void init() {
        if (!init) {
            QuartzJobScheduler.LOCAL.schedule(QuartzJobScheduler.createRecoverableJobBuilder(ProcessItemHourTrendingStatsJob.class), QuartzUtil.makeMinutelyTrigger(5));
            init = true;

            if (NetworkRegistry.getInstance().isWebapp()) {
                IPUtil.EndOfX.endOfAppComing.addRunnable("50FlushHourStatistics", () -> {
                    // we need to clear out the queue so lets remove the 5-minute task from above and then just run now
                    QuartzJobScheduler.LOCAL.remove(ProcessItemHourTrendingStatsJob.class.getSimpleName());
                    // now lets add a task for running right now
                    TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                        protected Object doMonitoredTask() {
                            ProcessItemHourTrendingStatsJob.processStats();
                            return null;
                        }
                    });
                });
            }
        }
    }

    static int getQueueSize() {
        return statQueue.size();
    }

    static ItemHourTrendingStats pollStatQueue() {
        return statQueue.poll();
    }
}
