package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("RewardPeriodStats")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class RewardPeriodStatsDTO implements Serializable {
    private static final long serialVersionUID = 5760117892558031040L;

    private final String rewardPeriodRange;

    private final NrveUsdValue contentCreatorReward;
    private final NrveUsdValue narrativeCompanyReward;
    private final NrveUsdValue nicheOwnershipReward;
    private final NrveUsdValue activityRewards;
    private final NrveUsdValue nicheModerationReward;
    private final NrveUsdValue electorateReward;
    private final NrveUsdValue tribunalReward;
    private final NrveUsdValue totalRewards;

    private final NrveUsdValue nicheOwnershipFeeRevenue;
    private final NrveUsdValue publicationOwnershipFeeRevenue;
    private final NrveUsdValue tokenMintRevenue;
    private final NrveUsdValue advertisingRevenue;
    private final NrveUsdValue miscellaneousRevenue;
    private final NrveUsdValue carryoverRevenue;
    private final NrveUsdValue totalRevenue;
}
