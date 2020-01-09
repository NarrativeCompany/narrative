package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.rewards.dao.NarrativeCompanyRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheModeratorRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheOwnerRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.RewardTransactionRefDAO;
import org.narrative.network.core.narrative.rewards.dao.RoleContentRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserElectorateRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserTribunalRewardDAO;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.rewards.services.slices.ContentCreatorsRewardSliceProcessor;
import org.narrative.network.core.narrative.rewards.services.slices.NarrativeCompanyRewardSliceProcessor;
import org.narrative.network.core.narrative.rewards.services.slices.NicheModeratorsRewardSliceProcessor;
import org.narrative.network.core.narrative.rewards.services.slices.NicheOwnersRewardSliceProcessor;
import org.narrative.network.core.narrative.rewards.services.slices.RewardSliceProcessorBase;
import org.narrative.network.core.narrative.rewards.services.slices.UserActivityRewardSliceProcessor;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-15
 * Time: 16:21
 *
 * @author jonmark
 */
public enum RewardSlice implements IntegerEnum {
    CONTENT_CREATORS(0, 60, 60, WalletTransactionType.CONTENT_REWARD, RoleContentRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            return new ContentCreatorsRewardSliceProcessor(period, totalNrve);
        }
    }
    ,NARRATIVE_COMPANY(1, 15, 15, WalletTransactionType.NARRATIVE_COMPANY_REWARD, NarrativeCompanyRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            return new NarrativeCompanyRewardSliceProcessor(period, totalNrve);
        }
    }
    ,NICHE_OWNERS(2, 10, 10, WalletTransactionType.NICHE_OWNERSHIP_REWARD, NicheOwnerRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            return new NicheOwnersRewardSliceProcessor(period, totalNrve);
        }
    }
    ,NICHE_MODERATORS(3, 6, 6, WalletTransactionType.NICHE_MODERATION_REWARD, NicheModeratorRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            return new NicheModeratorsRewardSliceProcessor(period, totalNrve);
        }
    }
    ,USER_ACTIVITY(4, 9, 4, WalletTransactionType.ACTIVITY_REWARD, UserActivityRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            return new UserActivityRewardSliceProcessor(period, totalNrve);
        }
    }
    ,ELECTORATE(5, 0, 4, WalletTransactionType.ELECTORATE_REWARD, UserElectorateRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            throw UnexpectedError.getRuntimeException("Electorate rewards are not currently supported!");
        }
    }
    ,TRIBUNAL(6, 0, 1, WalletTransactionType.TRIBUNAL_REWARD, UserTribunalRewardDAO.class) {
        @Override
        public RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve) {
            throw UnexpectedError.getRuntimeException("Tribunal rewards are not currently supported!");
        }
    }
    ;

    private final int id;
    // jw: note: both of these are stored as 0.XX decimals with scale set to 2 and RoundingMode set to UNNECESSARY. this means that
    //     whoever calls this to do math with them will need to ensure that they set proper and expected values for both.
    private final BigDecimal currentPercent;
    private final BigDecimal futurePercent;
    private final WalletTransactionType walletTransactionType;
    private final Class<? extends RewardTransactionRefDAO> rewardTransactionRefDaoClass;

    RewardSlice(int id, int currentPercent, int futurePercent, WalletTransactionType walletTransactionType, Class<? extends RewardTransactionRefDAO> rewardTransactionRefDaoClass) {
        this.id = id;
        this.currentPercent = getSlicePercentage(currentPercent);
        this.futurePercent = getSlicePercentage(futurePercent);
        this.walletTransactionType = walletTransactionType;
        this.rewardTransactionRefDaoClass = rewardTransactionRefDaoClass;
    }

    public static final Set<WalletTransactionType> ALL_REWARD_TRANSACTION_TYPES = Collections.unmodifiableSet(EnumSet.allOf(RewardSlice.class).stream().map(RewardSlice::getWalletTransactionType).collect(Collectors.toSet()));

    private static BigDecimal getSlicePercentage(int percent) {
        assert percent >= 0 || percent < 100 : "Every slice should be somewhere between 0 and 99. No slice should be 100% of the pie.";

        if (percent == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentBd = BigDecimal.valueOf(percent).setScale(2, RoundingMode.UNNECESSARY);

        // jw: no rounding should be necessary since 1-99 should never yield a number with infinite decimals
        return percentBd.divide(BigDecimal.valueOf(100), RoundingMode.UNNECESSARY);
    }

    @Override
    public int getId() {
        return id;
    }

    public BigDecimal getCurrentPercent() {
        return currentPercent;
    }

    public BigDecimal getFuturePercent() {
        return futurePercent;
    }

    public WalletTransactionType getWalletTransactionType() {
        return walletTransactionType;
    }

    public RewardTransactionRefDAO<? extends RewardTransactionRef> getRewardTransactionRefDao() {
        return DAOImpl.getDAOFromDAOClass(rewardTransactionRefDaoClass);
    }

    public NrveValue getSliceNrve(NrveValue totalNrve) {
        return RewardUtils.calculateNrveShare(getCurrentPercent(), totalNrve, RewardUtils.ROUNDING_MODE);
    }

    public abstract RewardSliceProcessorBase getProcessor(RewardPeriod period, NrveValue totalNrve);
}