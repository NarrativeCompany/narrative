package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-06-05
 * Time: 13:50
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("RewardLeaderboardPost")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class RewardLeaderboardPostDTO {
    private final OID postOid;
    private final PostDTO post;
    private final NrveUsdValue reward;
}
