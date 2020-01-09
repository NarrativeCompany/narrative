package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.RewardsService;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodStatsDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Date: 2019-06-03
 * Time: 16:04
 *
 * @author brian
 */
@RestController
@RequestMapping("/rewards")
@Validated
public class RewardsController {
    public static final String MONTH_PARAM = "month";
    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping(path = "/all-time-rewards")
    public ScalarResultDTO<NrveUsdValue> getAllTimeRewardsDisbursed() {
        return rewardsService.getAllTimeRewardsDisbursed();
    }

    @GetMapping(path = "/completed-periods")
    public List<RewardPeriodDTO> getAllCompletedRewardPeriods() {
        return rewardsService.getAllCompletedRewardPeriods();
    }

    @GetMapping(path = "/period-stats")
    public RewardPeriodStatsDTO getRewardPeriodStats(@RequestParam(name=MONTH_PARAM, required = false) String yearMonthStr) {
        return rewardsService.getRewardPeriodStats(yearMonthStr);
    }
}
