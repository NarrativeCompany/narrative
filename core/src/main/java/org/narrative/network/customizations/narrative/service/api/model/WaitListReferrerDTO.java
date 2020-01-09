package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 8/22/18
 * Time: 9:26 AM
 */
@JsonValueObject
@JsonTypeName("WaitListReferrer")
@Value
@Builder(toBuilder = true)
public class WaitListReferrerDTO {
    private final int confirmedReferralCount;
    private final UserDTO referrer;
}
