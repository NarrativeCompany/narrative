package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import lombok.Builder;
import lombok.Value;

/**
 * DTO that represents personal settings for a user.
 *
 * Date: 2019-03-07
 * Time: 08:12
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("UserPersonalSettings")
@Value
@Builder(toBuilder = true)
public class UserPersonalSettingsDTO {
    private final QualityFilter qualityFilter;
    private final boolean displayAgeRestrictedContent;
    private final boolean hideMyFollowers;
    private final boolean hideMyFollows;
}
