package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.customizations.narrative.service.api.StatsService;
import org.narrative.network.customizations.narrative.service.api.model.NicheStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.StatsOverviewDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Date: 10/24/18
 * Time: 11:11 PM
 *
 * @author brian
 */
@RestController
@RequestMapping("/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping(path = "/overview")
    public StatsOverviewDTO getStatsOverview() {
        return statsService.getStatsOverview();
    }

    @GetMapping(path = "/niches")
    public NicheStatsDTO getNicheStats() {
        return statsService.getNicheStats();
    }
}
