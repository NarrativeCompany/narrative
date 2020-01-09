package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 9/17/18
 * Time: 9:15 AM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("UserReferralDetails")
@Value
@Builder(toBuilder = true)
public class UserReferralDetailsDTO {
    private final Integer rank;
    private final int friendsJoined;
    private final NrveValue nrveEarned;
}
