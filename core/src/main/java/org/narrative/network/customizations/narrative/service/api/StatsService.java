package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.customizations.narrative.service.api.model.NicheStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.StatsOverviewDTO;

/**
 * Date: 11/28/18
 * Time: 7:26 AM
 *
 * @author brian
 */
public interface StatsService {
    /**
     * get an overview of network-wide statistics
     * @return {@link StatsOverviewDTO} containing network-wide statistics
     */
    StatsOverviewDTO getStatsOverview();

    NicheStatsDTO getNicheStats();
}
