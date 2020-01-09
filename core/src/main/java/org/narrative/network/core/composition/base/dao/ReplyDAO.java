package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEventType;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.shared.daobase.CompositionDAOImpl;
import org.narrative.network.shared.replies.services.ReplySortOrder;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkCoreUtils;
import org.hibernate.Query;
import org.hibernate.query.NativeQuery;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:38:43 PM
 */
public class ReplyDAO extends CompositionDAOImpl<Reply, OID> {
    public ReplyDAO() {
        super(Reply.class);
    }

    public int getPageNumberForReply(ReplySortOrder sort, Reply reply, int itemsPerPage, int offset, int minimumScore) {
        Query query;
        if (sort.isOldestToNewest()) {
            query = getGSession().getNamedQuery("reply.getReplyCountBefore");

        } else if (sort.isNewestToOldest()) {
            query = getGSession().getNamedQuery("reply.getReplyCountAfter");

        } else {
            assert sort.isPopular() : "Encountered unexpected sort type: " + sort;
            query = getGSession().getNamedQuery("reply.getPopularReplyCountAfter").setParameter("likeCount", reply.getReplyStats().getLikeCount());
        }

        int count = ((Number) query
                .setParameter("threadingOrder", reply.getThreadingOrder())
                .setParameter("composition", reply.getComposition())
                .setParameter("minimumScore", minimumScore)
                .uniqueResult()).intValue() + 1 + offset;

        return (int) (Math.ceil((double) count / (double) itemsPerPage));
    }

    public List<Reply> getSortedForComposition(ReplySortOrder sort, Composition composition, int start, int max, int minQualityScore) {
        final Query query;
        if (sort.isNewestToOldest()) {
            query = getGSession().getNamedQuery("reply.getOrderedNewestToOldestForComposition");

        } else if (sort.isOldestToNewest()) {
            query = getGSession().getNamedQuery("reply.getOrderedOldestToNewestForComposition");

        } else {
            query = getGSession().getNamedQuery("reply.getOrderedPopularForComposition");
        }

        return (List<Reply>) query
                .setParameter("composition", composition)
                .setParameter("minQualityScore", minQualityScore)
                .setFirstResult(start)
                .setMaxResults(max)
                .list();
    }

    public List<OID> getAllOidsForCompositionOids(Collection<OID> compositionOids) {
        return (List<OID>) getGSession().getNamedQuery("reply.getAllOidsForCompositionOids").setParameterList("compositionOids", compositionOids).list();
    }

    public List<Reply> getAllForCompositionOids(Collection<OID> compositionOids) {
        if (isEmptyOrNull(compositionOids)) {
            return Collections.emptyList();
        }
        return getGSession().getNamedQuery("reply.getAllForCompositionOids").setParameterList("compositionOids", compositionOids).list();
    }

    public List<Reply> getRepliesForReferendum(Referendum referendum, int lastLikeCount, Timestamp lastReplyDatetime, int maxItems) {
        return getGSession().getNamedQuery("reply.getRepliesForReferendum").setParameter("composition", Composition.dao().get(referendum.getOid())).setParameter("lastLikeCount", lastLikeCount).setParameter("lastReplyDatetime", lastReplyDatetime).setMaxResults(maxItems).list();
    }

    public Reply getForFilePointerSet(FilePointerSet filePointerSet) {
        return getFirstBy(new NameValuePair<>(Reply.FIELD__FILE_POINTER_SET__NAME, filePointerSet));
    }

    public void primeUsersForReplies(Collection<Reply> replies) {
        if (replies == null || replies.isEmpty()) {
            return;
        }
        Collection<OID> userOids = newHashSet();
        for (Reply reply : replies) {
            if (reply.getUserOid() != null) {
                userOids.add(reply.getUserOid());
            }
        }
        User.dao().primeObjectsNotInCache(userOids);
    }

    public List<Reply> getViewableRepliesForReplyOids(Collection<OID> replyOids) {
        return (List<Reply>) getGSession().getNamedQuery("reply.getViewableRepliesForReplyOids").setParameterList("replyOids", replyOids).list();

    }

    public List<Reply> getLastPublicRepliesForComposition(Composition composition, int count) {
        return getGSession().getNamedQuery("reply.getLastPublicRepliesForComposition").setParameter("composition", composition).setParameter("moderatedStatusType", ModerationStatus.MODERATED).setMaxResults(count).list();
    }

    public boolean updateCompositionStatsForReplyVisibilityChange(Reply reply, boolean isDelete) {
        final CompositionStats compositionStats = CompositionStats.dao().getLocked(reply.getComposition().getOid());

        // jw: We MUST initialize the composition before short circuiting out of this method for brians reasons below
        //     so leave these two lines together, please!
        if (isDelete) {
            compositionStats.setLastReply(null);
            Reply.dao().delete(reply);
        }
        // bl: this magical line is needed in order to avoid the ever annoying "collection was not processed by flush()"
        // error from Hibernate.  still not entirely sure why the Composition was having so many issues,
        // but simply initializing the Composition object before we do the subsequent query solves the issue.
        // this issue was preventing reply deletes.
        HibernateUtil.initializeObject(reply.getComposition());

        if (isDelete) {
            compositionStats.removeReply(reply);
        }

        List<Reply> updatedLastReplies = Reply.dao().getLastPublicRepliesForComposition(reply.getComposition(), 1);

        // bl: if there are no more replies, then set it to null
        Reply lastReplyForType = updatedLastReplies.size() > 0 ? updatedLastReplies.get(0) : null;
        compositionStats.setLastReply(lastReplyForType);

        // bl: if there weren't any last replies, then we just reset the CompositionStats.lastUpdateDatetime
        // from the corresponding CompositionConsumer.
        if (updatedLastReplies.isEmpty()) {
            switch (compositionStats.getComposition().getCompositionType()) {
                case CONTENT:
                    final Composition composition = reply.getComposition();
                    NetworkCoreUtils.networkContext().doAreaTask(composition.getArea(), new AreaTaskImpl<Object>() {
                        @Override
                        protected Object doMonitoredTask() {
                            compositionStats.setLastUpdateDatetime(Content.dao().get(composition.getOid()).getLiveDatetime());

                            return null;
                        }
                    });
                    break;
                case REFERENDUM:
                    compositionStats.setLastUpdateDatetime(Referendum.dao().get(compositionStats.getOid()).getStartDatetime());
                    break;
                default:
                    throw UnexpectedError.getRuntimeException("Unknown CompositionType '" + compositionStats.getComposition().getCompositionType() + "' for ReplyDAO.updateCompositionStatsForReply()");
            }
        } else {
            // bl: just use the first reply from the list since it should always be the most recent!
            compositionStats.setLastUpdateDatetime(updatedLastReplies.get(0).getLiveDatetime());
        }

        return true;
    }

    public int getReplyCountForAuthorAndConsumerAfterDate(User author, CompositionConsumer consumer, Instant afterDate) {
        return ((Number) getGSession().getNamedQuery("reply.getReplyCountForAuthorAndCompositionAfterDate")
                .setParameter("compositionOid", consumer.getOid())
                .setParameter("userOid", author.getOid())
                .setParameter("afterDate", new Date(afterDate.toEpochMilli()))
                .uniqueResult()).intValue();
    }

    public int getLiveReplyCountWithMinimumQuality(Composition composition, int minQualityScore) {
        return ((Number)getGSession().getNamedQuery("reply.getLiveReplyCountWithMinimumQuality")
                .setParameter("composition", composition)
                .setParameter("minQualityScore", minQualityScore)
                .uniqueResult()).intValue();
    }

    @Override
    public void save(Reply reply) {
        // bl: when saving Reply objects, we need to save, then flush, then refresh to get the threadingOrder
        super.save(reply);
        getGSession().flushSession();
        refresh(reply);
    }

    public int createTemporaryUserActivityPointsTable() {
        // bl: this temp table is only associated with Reply because i needed to put it somewhere. the name is funky
        // but it gets the job done.
        return getGSession().getNamedQuery("userActivityReward.reply.createTemporaryUserActivityPointsTable")
                .executeUpdate();
    }

    public int dropTemporaryUserActivityPointsTable() {
        // bl: this temp table is only associated with Reply because i needed to put it somewhere. the name is funky
        // but it gets the job done.
        return getGSession().getNamedQuery("userActivityReward.reply.dropTemporaryUserActivityPointsTable")
                .executeUpdate();
    }

    public int insertTempUserActivityPointRecordsForSubmitComment(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.reply.insertTempUserActivityPointRecordsForSubmitComment")
                .setParameter("minScore", QualityLevel.MEDIUM.getMinimumScore())
                .setParameter("approvedModerationStatus", ModerationStatus.APPROVED.getIdStr());
        return UserActivityRewardDAO.insertTempRecords(rewardPeriod, UserActivityRewardEventType.SUBMIT_COMMENT, false, query);
    }

    public int insertTempUserActivityPointRecordsForRatePostComment(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.reply.insertTempUserActivityPointRecordsForRatePostComment");
        return UserActivityRewardDAO.insertTempRecords(rewardPeriod, UserActivityRewardEventType.RATE_POST_COMMENT, false, query);
    }

    public List<ObjectTriplet<OID, Long, Boolean>> getTempUserActivityPointRecords(int page, int rowsPerPage) {
        List<Object[]> vals = getGSession().getNamedNativeQuery("userActivityReward.reply.getTempUserActivityPointRecords")
                .setMaxResults(rowsPerPage)
                .setFirstResult((page-1)*rowsPerPage)
                .list();
        List<ObjectTriplet<OID, Long, Boolean>> ret = new ArrayList<>(vals.size());
        for (Object[] val : vals) {
            ret.add(new ObjectTriplet<>(OID.valueOf((Number)val[0]), ((Number)val[1]).longValue(), (Boolean) val[2]));
        }
        return ret;
    }

    public long getCountCreatedByUserAfter(User user, Instant after) {
        return getGSession().createNamedQuery("reply.getCountCreatedByUserAfter", Number.class)
                .setParameter("userOid", user.getOid())
                .setParameter("after", new Date(after.toEpochMilli()))
                .uniqueResult().longValue();
    }
}
