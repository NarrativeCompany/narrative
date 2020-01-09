package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.util.List;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("UserRewardPeriodStats")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class UserRewardPeriodStatsDTO {
    private final String rewardPeriodRange;

    private final NrveUsdValue totalContentCreationReward;
    private final NrveUsdValue totalNicheOwnershipReward;
    private final NrveUsdValue totalNicheModerationReward;
    private final NrveUsdValue totalActivityRewards;
    private final int activityBonusPercentage;
    private final NrveUsdValue totalElectorateReward;
    private final NrveUsdValue totalTribunalReward;
    private final NrveUsdValue totalReward;

    private final List<NicheOwnershipRewardDTO> nicheOwnershipRewards;

    private final BigDecimal percentageOfTotalPayout;
}
