package org.narrative.network.customizations.narrative.service.impl.rewards;

import org.narrative.common.util.InvalidParamError;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.controller.RewardsController;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.RewardsService;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodStatsDTO;
import org.narrative.network.customizations.narrative.service.mapper.RewardPeriodMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-03
 * Time: 16:03
 *
 * @author brian
 */
@Service
public class RewardsServiceImpl implements RewardsService {
    private final RewardPeriodMapper rewardPeriodMapper;

    public RewardsServiceImpl(RewardPeriodMapper rewardPeriodMapper) {
        this.rewardPeriodMapper = rewardPeriodMapper;
    }

    @Override
    public ScalarResultDTO<NrveUsdValue> getAllTimeRewardsDisbursed() {
        NrveValue allTimeRewards = RewardPeriod.dao().getAllTimeRewardsDisbursed();
        return ScalarResultDTO.<NrveUsdValue>builder().value(new NrveUsdValue(allTimeRewards)).build();
    }

    @Override
    public List<RewardPeriodDTO> getAllCompletedRewardPeriods() {
        List<RewardPeriod> rewardPeriods = RewardPeriod.dao().getAllCompletedPeriods();
        return rewardPeriodMapper.mapRewardPeriodEntityListToRewardPeriodDTOList(rewardPeriods);
    }

    @Override
    public RewardPeriod getRewardPeriodFromParam(String yearMonthStr, boolean defaultToMostRecent) throws InvalidParamError {
        // bl: default to the most recently completed rewards month
        if(isEmpty(yearMonthStr)) {
            if(defaultToMostRecent) {
                return RewardPeriod.dao().getAllCompletedPeriods(1).get(0);
            }

            // if not defaulting to most recent, then return null
            return null;
        }

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(yearMonthStr);
        } catch(DateTimeParseException e) {
            yearMonth = null;
        }
        // bl: the YearMonth must always be in the past
        if(yearMonth==null || !yearMonth.isBefore(RewardUtils.nowYearMonth())) {
            throw new InvalidParamError(RewardsController.MONTH_PARAM, yearMonthStr);
        }
        RewardPeriod rewardPeriod = RewardPeriod.dao().getForYearMonth(yearMonth);
        if(!exists(rewardPeriod) || !rewardPeriod.isCompleted()) {
            throw new InvalidParamError(RewardsController.MONTH_PARAM, yearMonthStr);
        }
        return rewardPeriod;
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_REWARDSSERVICE_REWARD_PERIOD_STATS)
    @Override
    public RewardPeriodStatsDTO getRewardPeriodStats(String yearMonthStr) {
        RewardPeriod rewardPeriod = getRewardPeriodFromParam(yearMonthStr, true);

        // bl: so that the math adds up (given that we round everything down to the nearest NRVE), calculate a new total to use
        NrveValue totalRewards = NrveValue.ZERO;
        Map<RewardSlice,NrveUsdValue> sliceToAmount = new HashMap<>();
        {
            NrveValue rewardPeriodRewardsDisbursed = rewardPeriod.getTotalRewardsDisbursed();
            for (RewardSlice slice : RewardSlice.values()) {
                NrveValue sliceValue = slice.getSliceNrve(rewardPeriodRewardsDisbursed);
                NrveUsdValue nrveUsdValue = new NrveUsdValue(sliceValue);
                sliceToAmount.put(slice, nrveUsdValue);
                totalRewards = totalRewards.add(nrveUsdValue.getNrve());
            }
        }

        NrveValue totalRevenue = NrveValue.ZERO;
        NrveValue miscellaneousRevenue = NrveValue.ZERO;
        Map<WalletTransactionType,NrveUsdValue> revenueTypeToNrveUsd = new HashMap<>();
        {
            Map<WalletTransactionType,NrveValue> revenueTypeToAmount = WalletTransaction.dao().getTransactionSumsByTypeToWallet(rewardPeriod.getWallet(), WalletTransactionType.REVENUE_TYPES);
            for (Map.Entry<WalletTransactionType, NrveValue> entry : revenueTypeToAmount.entrySet()) {
                WalletTransactionType type = entry.getKey();
                NrveValue nrveValue = entry.getValue();
                NrveUsdValue nrveUsdValue = new NrveUsdValue(nrveValue);
                totalRevenue = totalRevenue.add(nrveUsdValue.getNrve());
                if(type.isMiscellaneousRevenue()) {
                    miscellaneousRevenue = miscellaneousRevenue.add(nrveUsdValue.getNrve());
                } else {
                    revenueTypeToNrveUsd.put(type, nrveUsdValue);
                }
            }
        }

        return RewardPeriodStatsDTO.builder()

                .rewardPeriodRange(rewardPeriod.getRewardYearMonth().getRewardPeriodRange())

                // reward distributions
                .contentCreatorReward(sliceToAmount.get(RewardSlice.CONTENT_CREATORS))
                .narrativeCompanyReward(sliceToAmount.get(RewardSlice.NARRATIVE_COMPANY))
                .nicheOwnershipReward(sliceToAmount.get(RewardSlice.NICHE_OWNERS))
                .activityRewards(sliceToAmount.get(RewardSlice.USER_ACTIVITY))
                .nicheModerationReward(sliceToAmount.get(RewardSlice.NICHE_MODERATORS))
                .electorateReward(sliceToAmount.get(RewardSlice.ELECTORATE))
                .tribunalReward(sliceToAmount.get(RewardSlice.TRIBUNAL))
                .totalRewards(new NrveUsdValue(totalRewards))

                // revenue
                .nicheOwnershipFeeRevenue(revenueTypeToNrveUsd.get(WalletTransactionType.PRORATED_NICHE_MONTH_REVENUE))
                .publicationOwnershipFeeRevenue(revenueTypeToNrveUsd.get(WalletTransactionType.PRORATED_PUBLICATION_MONTH_REVENUE))
                .tokenMintRevenue(revenueTypeToNrveUsd.get(WalletTransactionType.MINTED_TOKENS))
                .advertisingRevenue(revenueTypeToNrveUsd.get(WalletTransactionType.ADVERTISING_REVENUE))
                .miscellaneousRevenue(new NrveUsdValue(miscellaneousRevenue))
                .carryoverRevenue(revenueTypeToNrveUsd.get(WalletTransactionType.REWARD_PERIOD_CARRYOVER))
                .totalRevenue(new NrveUsdValue(totalRevenue))

                .build();
    }
}
