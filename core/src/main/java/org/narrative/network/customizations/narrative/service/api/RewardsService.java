package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.util.InvalidParamError;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodStatsDTO;

import java.util.List;

/**
 * Date: 11/28/18
 * Time: 7:26 AM
 *
 * @author brian
 */
public interface RewardsService {
    /**
     * get the balance of all-time rewards disbursed
     * @return a {@link ScalarResultDTO} containing the {@link NrveUsdValue} of all-time rewards disbursed
     */
    ScalarResultDTO<NrveUsdValue> getAllTimeRewardsDisbursed();

    /**
     * get a list of all completed RewardPeriods
     * @return a list of all completed {@link RewardPeriodDTO}s
     */
    List<RewardPeriodDTO> getAllCompletedRewardPeriods();

    /**
     * get the {@link RewardPeriod} give a month param from the API
     * @param yearMonthStr the month string in YYYY-MM format. optional.
     * @param defaultToMostRecent true if you want the most recent {@link RewardPeriod} returned in the event the param is unspecified.
     * @return the {@link RewardPeriod} corresponding to the parameter.
     * @throws {@link InvalidParamError} if the RewardPeriod doesn't exist or isn't completed yet.
     */
    RewardPeriod getRewardPeriodFromParam(String yearMonthStr, boolean defaultToMostRecent) throws InvalidParamError;

    /**
     * get a breakdown of all revenue and payouts for a given RewardPeriod
     * @param yearMonthStr the YearMonth to get rewards for
     * @return {@link RewardPeriodStatsDTO} containing stats for the given RewardPeriod
     */
    RewardPeriodStatsDTO getRewardPeriodStats(String yearMonthStr);
}
