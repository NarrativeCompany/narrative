package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.content.base.ContentStatus;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.UserActivityBonus;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEventType;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.customizations.narrative.elections.ElectionType;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.hibernate.query.NativeQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-13
 * Time: 15:00
 *
 * @author jonmark
 */
public class UserActivityRewardDAO extends RewardTransactionRefDAO<UserActivityReward> {
    public UserActivityRewardDAO() {
        super(UserActivityReward.class);
    }

    public int createTemporaryUserActivityPointsTable() {
        return getGSession().getNamedQuery("userActivityReward.createTemporaryUserActivityPointsTable")
                .executeUpdate();
    }

    public int dropTemporaryUserActivityPointsTable() {
        return getGSession().getNamedQuery("userActivityReward.dropTemporaryUserActivityPointsTable")
                .executeUpdate();
    }

    public int insertTempRecordsForPublishPost(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForPublishPost")
                .setParameter("minScore", QualityLevel.MEDIUM.getMinimumScore())
                .setParameter("activeContentStatus", ContentStatus.ACTIVE.getBitmask())
                .setParameter("approvedModerationStatus", ModerationStatus.APPROVED.getIdStr());
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.PUBLISH_POST, false, query);
    }

    public int insertTempRecordsForVotes(RewardPeriod rewardPeriod) {
        return insertTempRecordsForReferendumVotes(
                rewardPeriod,
                ReferendumType.NICHE_TYPES,
                UserActivityRewardEventType.VOTE
        );
    }

    public int insertTempRecordsForAppealVotes(RewardPeriod rewardPeriod) {
        return insertTempRecordsForReferendumVotes(
                rewardPeriod,
                ReferendumType.TRIBUNAL_TYPES,
                UserActivityRewardEventType.VOTE_ON_APPEAL
        );
    }

    private int insertTempRecordsForReferendumVotes(RewardPeriod rewardPeriod, Collection<ReferendumType> referendumTypes, UserActivityRewardEventType userActivityRewardEventType) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForReferendumVotes")
                .setParameterList("referendumTypes", referendumTypes.stream().map(ReferendumType::getId).toArray());
        return insertTempRecords(rewardPeriod, userActivityRewardEventType, false, query);
    }

    public int insertTempRecordsForNicheAuctionBids(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForNicheAuctionBids");
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.BID_ON_AUCTION, true, query);
    }

    public int insertTempRecordsForNicheAuctionWins(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForNicheAuctionWins");
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.WIN_NICHE_AUCTION, false, query);
    }

    public int insertTempRecordsForSuggestedNicheApprovals(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForSuggestedNicheApprovals")
                .setParameterList("approvalReferendumTypes", Arrays.asList(
                        ReferendumType.APPROVE_SUGGESTED_NICHE.getId()
                        ,ReferendumType.APPROVE_REJECTED_NICHE.getId()
                        ,ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE.getId()
                ));
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.SUGGESTED_NICHE_APPROVED, false, query);
    }

    public int insertTempRecordsForUserFollows(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForUserFollows");
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.FOLLOW_SOMETHING, false, query);
    }

    public int insertTempRecordsForChannelFollows(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForChannelFollows");
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.FOLLOW_SOMETHING, true, query);
    }

    public int insertTempRecordsForAcceptedModeratorNominations(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForAcceptedModeratorNominations")
                .setParameter("confirmedNomineeStatus", NomineeStatus.CONFIRMED.getId())
                .setParameter("nicheModeratorElectionType", ElectionType.NICHE_MODERATOR.getId());
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.ACCEPT_MODERATOR_NOMINATION, true, query);
    }

    public int insertTempRecordsForCertifications(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForCertifications")
                .setParameter("approvedKycStatus", UserKycStatus.APPROVED.getId());
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.GET_CERTIFIED, false, query);
    }

    public int insertTempRecordsForProfilePictureUpload(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForProfilePictureUpload");
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.ADD_PROFILE_PICTURE, true, query);
    }

    public int insertTempRecordsForNicheAuctionPayments(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForNicheAuctionPayments")
                .setParameter("nicheAuctionInvoiceType", InvoiceType.NICHE_AUCTION.getId())
                .setParameter("paidInvoiceStatus", InvoiceStatus.PAID.getId());
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.PAY_FOR_NICHE, false, query);
    }

    public int insertTempRecordsForUpheldAppeals(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForUpheldAppeals")
                .setParameterList("appealTypes", TribunalIssueType.APPEAL_TYPES.stream().map(TribunalIssueType::getId).toArray())
                .setParameter("approveRejectedNicheType", TribunalIssueType.APPROVE_REJECTED_NICHE.getId());
        return insertTempRecords(rewardPeriod, UserActivityRewardEventType.APPEAL_UPHELD, false, query);
    }

    public int insertTempRecordsForRewardEvents(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.insertTempRecordsForRewardEvents")
                .setParameterList("eventTypes", UserActivityRewardEventType.TYPES_FOR_EVENTS.stream().map(UserActivityRewardEventType::getId).toArray());
        // bl: pass in all of the types to the query so we know how many points each is worth
        // and whether it is reputation adjusted or not
        for (UserActivityRewardEventType type : UserActivityRewardEventType.TYPES_FOR_EVENTS) {
            query
                    .setParameter(type.name(), type.getId())
                    .setParameter(type.name() + "_points", type.getPoints())
                    .setParameter(type.name() + "_reputationAdjusted", type.isReputationAdjusted() ? 1 : 0);
        }
        return setBoundParameters(rewardPeriod, true, query)
                .executeUpdate();
    }

    public static int insertTempRecords(RewardPeriod rewardPeriod, UserActivityRewardEventType type, boolean useInstant, NativeQuery query) {
        return setBoundParameters(rewardPeriod, useInstant, query)
                .setParameter("activityPoints", type.getPoints())
                .executeUpdate();
    }

    private static NativeQuery setBoundParameters(RewardPeriod rewardPeriod, boolean useInstant, NativeQuery query) {
        long lowerBound = rewardPeriod.getRewardYearMonth().getLowerBoundForQuery().toEpochMilli();
        long upperBound = rewardPeriod.getRewardYearMonth().getUpperBoundForQuery().toEpochMilli();
        return query
                .setParameter("lowerBound", useInstant ? lowerBound : new Date(lowerBound))
                .setParameter("upperBound", useInstant ? upperBound : new Date(upperBound));
    }

    public int insertUserActivityRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedNativeQuery("userActivityReward.insertUserActivityRewardsForPeriod")
                .setParameter("noneBonus", UserActivityBonus.NONE.getId())
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("deletedUserStatus", UserStatus.DELETED.getBitmask())
                .executeUpdate();
    }

    public int updateUserActivityRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedNativeQuery("userActivityReward.updateUserActivityRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .executeUpdate();
    }

    public List<OID> getAllUserOidsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().createNamedQuery("userActivityReward.getAllUserOidsForPeriod", OID.class)
                .setParameter("rewardPeriod", rewardPeriod)
                .list();
    }

    public List<UserReputation> getAllReputationDataEagerFetchForUserOids(List<OID> userOids) {
        // bl: eagerly fetch all of the data so we have it in the Hibernate session.
        // bl: fetching as an array so that we get all of the fields, not just the OIDs
        List<Object[]> list = getGSession().getNamedQuery("userActivityReward.getAllReputationDataEagerFetchForUserOids")
                .setParameterList("userOids", userOids)
                .list();

        // just return the easier-to-consume list of UserReputation objects
        List<UserReputation> userReputations = new ArrayList<>(list.size());
        for (Object[] objs : list) {
            userReputations.add((UserReputation)objs[0]);
        }
        return userReputations;
    }

    public int applyReputationAdjustmentToPointsForPeriod(RewardPeriod rewardPeriod, BigDecimal reputationMultiplier, UserActivityBonus activityBonus, Set<OID> userOids) {
        return getGSession().getNamedNativeQuery("userActivityReward.applyReputationAdjustmentToPointsForPeriod")
                .setParameter("reputationMultiplier", reputationMultiplier.doubleValue())
                .setParameter("activityBonus", activityBonus.getId())
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameterList("userOids", userOids.stream().map(OID::getValue).toArray())
                .executeUpdate();
    }

    public int updateUserActivityRewardsForFounderBonus(RewardPeriod rewardPeriod) {
        Area narrativePlatformArea = Area.dao().getNarrativePlatformArea();
        SandboxedCommunitySettings settings = narrativePlatformArea.getAuthZone().getSandboxedCommunitySettings();
        AreaCircle foundersAreaCircle = settings.getCirclesByNarrativeCircleType().get(NarrativeCircleType.FOUNDING_MEMBERS);
        assert exists(foundersAreaCircle) : "Should always find the Founding Members circle!";

        return getGSession().getNamedQuery("userActivityReward.updateUserActivityRewardsForFounderBonus")
                .setParameter("narrativePlatformAreaOid", narrativePlatformArea.getOid())
                .setParameter("foundersAreaCircleOid", foundersAreaCircle.getOid())
                .setParameter("founderMultiplier", UserActivityReward.FOUNDERS_BONUS_MULTIPLIER)
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .executeUpdate();
    }

    public int applyUserActivityRewardReputationBonuses(RewardPeriod rewardPeriod) {
        NativeQuery query = getGSession().getNamedNativeQuery("userActivityReward.applyUserActivityRewardReputationBonuses")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameterList("bonusTiers", UserActivityBonus.BONUS_TIERS.stream().map(UserActivityBonus::getId).toArray());

        for (UserActivityBonus tier : UserActivityBonus.BONUS_TIERS) {
            query
                    .setParameter(tier.name(), tier.getId())
                    .setParameter(tier.name() + "_multiplier", tier.getBonusMultiplier().toPlainString());
        }

        return query.executeUpdate();
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        return getGSession().createNamedQuery("userActivityReward.getCountIncompleteRewardTransactionRefs", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("userActivityReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.USER_ACTIVITY.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public long getTotalPointsForPeriod(RewardPeriod period) {
        Number total = getGSession().createNamedQuery("userActivityReward.getTotalPointsForPeriod", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult();
        return total!=null ? total.longValue() : 0;
    }

    public List<UserActivityReward> getIncompleteUserActivityRewards(RewardPeriod period, int limit) {
        return getGSession().createNamedQuery("userActivityReward.getIncompleteUserActivityRewards", UserActivityReward.class)
                .setParameter("rewardPeriod", period)
                .setMaxResults(limit)
                .list();
    }

    public UserActivityReward getForUserRewardPeriod(User user, RewardPeriod rewardPeriod) {
        return getUniqueBy(new NameValuePair<>(UserActivityReward.Fields.user, user), new NameValuePair<>(UserActivityReward.Fields.period, rewardPeriod));
    }

    public Map<OID, UserActivityBonus> getTransactionOidToActivityBonus(Set<WalletTransaction> transactions) {
        List<ObjectPair<OID, UserActivityBonus>> pairs = getGSession().createNamedQuery("userActivityReward.getTransactionOidToActivityBonus", (Class<ObjectPair<OID, UserActivityBonus>>)(Class)ObjectPair.class)
                .setParameterList("transactions", transactions)
                .list();
        return ObjectPair.getAsMap(pairs);
    }
}
