package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("StatsOverview")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class StatsOverviewDTO implements Serializable {
    private static final long serialVersionUID = 2611371833065492118L;

    private final long totalMembers;
    private final Long uniqueVisitorsPast30Days;
    private final long activeMembersPast30Days;
    private final long nicheOwners;
    private final long approvedNiches;
    private final long activeNiches;
    private final NrveUsdValue networkRewardsPaidLastMonth;
    private final NrveUsdValue allTimeReferralRewards;

    private final long totalPosts;
    private final List<TopNicheDTO> topNiches;

    private final Date timestamp;
}
