package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("NicheRewardPeriodStats")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheRewardPeriodStatsDTO {
    private final String rewardPeriodRange;

    private final NrveUsdValue totalOwnerReward;
    private final NrveUsdValue totalModeratorReward;
    private final long totalQualifyingPosts;
}
