package org.narrative.network.customizations.narrative.controller.postbody.user;

import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-03-07
 * Time: 08:37
 *
 * @author brian
 */
@Value
@Builder
public class UserPersonalSettingsInputDTO {
    @NotNull
    private final QualityFilter qualityFilter;
    @NotNull
    private final boolean displayAgeRestrictedContent;
    @NotNull
    private final boolean hideMyFollowers;
    @NotNull
    private final boolean hideMyFollows;
}
