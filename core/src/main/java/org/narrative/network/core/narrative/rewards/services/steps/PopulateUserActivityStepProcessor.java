package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.UserActivityBonus;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEventType;
import org.narrative.network.customizations.narrative.reputation.ReputationMultiplierTier;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskIsolationLevel;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2019-05-27
 * Time: 16:52
 *
 * @author brian
 */
public class PopulateUserActivityStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(PopulateUserActivityStepProcessor.class);

    public PopulateUserActivityStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.POPULATE_USER_ACTIVITY);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: create a temporary table into which we will record ALL user activity. we'll insert new rows into
        // this table for each type of user activity and then eventually create UserActivityReward records
        // from the data in this table.
        // bl: first, create the temp table to insert into in each composition database
        // bl: in order to create the temp table, we need to commit the transaction so we are in
        // auto-commit mode. this is a requirement for GTID consistency used by Google Cloud SQL
        UserActivityReward.dao().runForAutoCommit(() -> UserActivityReward.dao().createTemporaryUserActivityPointsTable());

        // UserActivityRewardEventType.PUBLISH_POST
        {
            int count = UserActivityReward.dao().insertTempRecordsForPublishPost(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.PUBLISH_POST + " activity records.");
        }

        // bl: a bunch of inserts need to happen in Composition
        PartitionType.COMPOSITION.doTaskInAllPartitionsOfThisType(new TaskOptions(TaskIsolationLevel.ISOLATED_KEEP_CURRENT_SESSIONS), new AllPartitionsTask<Object>(true) {
            @Override
            protected Object doMonitoredTask() {
                // bl: first, create the temp table to insert into in each composition database
                // bl: in order to create the temp table, we need to commit the transaction so we are in
                // auto-commit mode. this is a requirement for GTID consistency used by Google Cloud SQL
                Reply.dao().runForAutoCommit(() -> Reply.dao().createTemporaryUserActivityPointsTable());

                // UserActivityRewardEventType.SUBMIT_COMMENT
                {
                    int count = Reply.dao().insertTempUserActivityPointRecordsForSubmitComment(period);
                    if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.SUBMIT_COMMENT + " activity records for " + getCurrentPartition().getDatabaseName() + ".");
                }

                // UserActivityRewardEventType.RATE_POST_COMMENT
                {
                    int count = Reply.dao().insertTempUserActivityPointRecordsForRatePostComment(period);
                    if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.RATE_POST_COMMENT + " activity records for " + getCurrentPartition().getDatabaseName() + ".");
                }

                // once we are done in the composition, chunk through to select all of the records to insert into global
                Connection con = UserActivityReward.dao().getGSession().getConnection();
                final int rowsPerPage = 50000;
                // bl: default pageResultCount to a full page for loop bootstrapping
                int pageResultCount = rowsPerPage;
                int page = 1;
                // bl: stop looping once we no longer get a full page of results
                while(rowsPerPage==pageResultCount) {
                    List<ObjectTriplet<OID, Long, Boolean>> results = Reply.dao().getTempUserActivityPointRecords(page, rowsPerPage);

                    // bl: for each chunk, generate one big bulk insert statement
                    if(!results.isEmpty()) {
                        StringBuilder valueClause = new StringBuilder();
                        for (ObjectTriplet<OID, Long, Boolean> result : results) {
                            if(valueClause.length() > 0) {
                                valueClause.append(",");
                            }
                            valueClause.append(PersistenceUtil.getBracketedCommaSeparatedValues(Arrays.asList(result.getOne(), result.getTwo(), result.getThree() ? 1 : 0)));
                        }
                        // bl: i don't particularly love this, but it works. another option would be to use
                        // federated tables since we're basically just joining between partitions. this seemed
                        // to be the most lightweight approach without requiring environmental and permission changes
                        String insertSql = "insert into tmp_UserActivityPoints (userOid, points, reputationAdjusted) values " + valueClause;

                        try {
                            // bl: using a raw connection and prepared statement so that it happens as part of the
                            // same Hibernate transaction the rewards are being processed on. need that so that
                            // we have access to the tmp_UserActivityPoints temporary table stored in this transaction's session.
                            con.prepareStatement(insertSql).executeUpdate();
                        } catch (SQLException e) {
                            throw UnexpectedError.getRuntimeException("Failed executing raw SQL. insertSql/" + insertSql, e);
                        }
                    }

                    pageResultCount = results.size();
                    page++;
                }

                // bl: now that we are done, drop the temporary table
                Reply.dao().dropTemporaryUserActivityPointsTable();
                return null;
            }
        });

        // UserActivityRewardEventType.VOTE
        {
            int count = UserActivityReward.dao().insertTempRecordsForVotes(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.VOTE + " activity records.");
        }

        // UserActivityRewardEventType.VOTE_ON_APPEAL
        {
            int count = UserActivityReward.dao().insertTempRecordsForAppealVotes(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.VOTE_ON_APPEAL + " activity records.");
        }

        // UserActivityRewardEventType.BID_ON_AUCTION
        {
            int count = UserActivityReward.dao().insertTempRecordsForNicheAuctionBids(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.BID_ON_AUCTION + " activity records.");
        }

        // UserActivityRewardEventType.WIN_NICHE_AUCTION
        {
            int count = UserActivityReward.dao().insertTempRecordsForNicheAuctionWins(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.WIN_NICHE_AUCTION + " activity records.");
        }

        // UserActivityRewardEventType.SUGGESTED_NICHE_APPROVED
        {
            int count = UserActivityReward.dao().insertTempRecordsForSuggestedNicheApprovals(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.SUGGESTED_NICHE_APPROVED + " activity records.");
        }

        // UserActivityRewardEventType.FOLLOW_SOMETHING
        {
            int count = UserActivityReward.dao().insertTempRecordsForUserFollows(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.FOLLOW_SOMETHING + " activity records for user follows.");
        }
        {
            int count = UserActivityReward.dao().insertTempRecordsForChannelFollows(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.FOLLOW_SOMETHING + " activity records for channel follows.");
        }

        // UserActivityRewardEventType.ACCEPT_MODERATOR_NOMINATION
        {
            int count = UserActivityReward.dao().insertTempRecordsForAcceptedModeratorNominations(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.ACCEPT_MODERATOR_NOMINATION + " activity records.");
        }

        // UserActivityRewardEventType.WIN_NICHE_MODERATOR_ELECTION
        // don't have elections yet, so can't implement this

        // UserActivityRewardEventType.GET_CERTIFIED
        {
            int count = UserActivityReward.dao().insertTempRecordsForCertifications(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.GET_CERTIFIED + " activity records.");
        }
        
        // UserActivityRewardEventType.ADD_PROFILE_PICTURE
        {
            int count = UserActivityReward.dao().insertTempRecordsForProfilePictureUpload(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.ADD_PROFILE_PICTURE + " activity records.");
        }
        
        // UserActivityRewardEventType.TIP_SOMEONE
        // don't have tips yet, so can't implement this

        // UserActivityRewardEventType.PROMOTE_POST
        // don't support promoting posts, so can't implement this

        // UserActivityRewardEventType.PAY_FOR_NICHE
        {
            int count = UserActivityReward.dao().insertTempRecordsForNicheAuctionPayments(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.PAY_FOR_NICHE + " activity records.");
        }

        // UserActivityRewardEventType.PAY_FOR_PUBLICATION
        // don't support publications, so can't implement this

        // UserActivityRewardEventType.APPEAL_UPHELD
        {
            int count = UserActivityReward.dao().insertTempRecordsForUpheldAppeals(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " " + UserActivityRewardEventType.APPEAL_UPHELD + " activity records.");
        }

        // UserActivityRewardEventType.REPORTED_AUP_VIOLATION_REMOVED
        // bl: this query generically supports any events that have to be recorded
        // into the UserActivityRewardEvent table because the data can't be derived through any
        // other mechanism.
        {
            int count = UserActivityReward.dao().insertTempRecordsForRewardEvents(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " events from UserActivityRewardEvent. Types: " + UserActivityRewardEventType.TYPES_FOR_EVENTS);
        }

        // FINALLY, we can insert the UserActivityReward records!
        // bl: note that the points value inserted here only includes values that should be reputation adjusted
        {
            int count = UserActivityReward.dao().insertUserActivityRewardsForPeriod(period);
            if(logger.isInfoEnabled()) logger.info("Inserted " + count + " UserActivityReward records.");
        }

        // now, reputation adjust the point values for every record
        // this should also set the UserActivityReward.bonus value for high rep users
        reputationAdjustPoints();

        // then, add the profile pic points (any non-reputation adjusted points)
        {
            int count = UserActivityReward.dao().updateUserActivityRewardsForPeriod(period);
            if(logger.isInfoEnabled()) logger.info("Updated " + count + " UserActivityReward records with non-reputation-adjusted points.");
        }

        // now, apply the Founder bonus
        {
            int count = UserActivityReward.dao().updateUserActivityRewardsForFounderBonus(period);
            if(logger.isInfoEnabled()) logger.info("Applied the Founders 10% bonus to " + count + " UserActivityReward records.");
        }

        // last but not least, apply the high reputation bonus
        {
            int count = UserActivityReward.dao().applyUserActivityRewardReputationBonuses(period);
            if(logger.isInfoEnabled()) logger.info("Applied the High Rep bonuses to " + count + " UserActivityReward records.");
        }

        // bl: finally, drop the temp table
        {
            UserActivityReward.dao().dropTemporaryUserActivityPointsTable();
            if(logger.isInfoEnabled()) logger.info("Dropped the tmp_UserActivityPoints table.");
        }

        return null;
    }

    private void reputationAdjustPoints() {
        // bl: group users by Reputation score so we can do one bulk update per reputation score. should result
        // in at most 101 update statements. given that we currently only have 7,500 members on narrative,
        // this should perform acceptably for the foreseeable future.
        Map<Integer, Set<OID>> reputationScoreToUserOids = new HashMap<>();

        List<OID> allUserOids = UserActivityReward.dao().getAllUserOidsForPeriod(period);
        // bl: chunk through the users to get reputation scores for each. 500 at a time to keep Hibernate sessions small.
        Iterator<List<OID>> iter = new SubListIterator<>(allUserOids, SubListIterator.CHUNK_MEDIUM);
        while (iter.hasNext()) {
            List<OID> oidChunk =  iter.next();
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(false) {
                @Override
                protected Object doMonitoredTask() {
                    // todo: why is this fetching all of the User data?! UserKyc.user is a required OneToOne that is lazy.
                    List<UserReputation> userReputations = UserActivityReward.dao().getAllReputationDataEagerFetchForUserOids(oidChunk);
                    for (UserReputation userReputation : userReputations) {
                        // bl: need to look up the UserReputation objects so that we can calculate the
                        // user's real reputation score, which is always derived in the java and not
                        // stored explicitly in the database due to KYC and Conduct Negative concerns.
                        int reputationScore = userReputation.getTotalScore();
                        Set<OID> userOidsForScore = reputationScoreToUserOids.computeIfAbsent(reputationScore, k -> new HashSet<>());
                        userOidsForScore.add(userReputation.getUserOid());
                    }
                    return null;
                }
            });
        }

        // bl: now issue an update statement for each reputation score that will reputation-adjust the points
        // and set the bonus value
        for (Map.Entry<Integer, Set<OID>> entry : reputationScoreToUserOids.entrySet()) {
            int reputationScore = entry.getKey();
            Set<OID> userOids = entry.getValue();
            ReputationMultiplierTier multiplierTier = ReputationMultiplierTier.getForScore(reputationScore);
            BigDecimal reputationMultiplier = multiplierTier.getMultiplier().multiply(BigDecimal.valueOf(reputationScore));
            UserActivityBonus activityBonus = multiplierTier.getActivityBonus();

            int count = UserActivityReward.dao().applyReputationAdjustmentToPointsForPeriod(period, reputationMultiplier, activityBonus, userOids);
            if(logger.isInfoEnabled()) logger.info("Updated " + count + " UserActivityReward records with reputation score of " + reputationScore + " multiplier/" + reputationMultiplier + " bonus/" + activityBonus + ".");
        }
    }
}
