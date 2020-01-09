package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Validated
@FieldNameConstants
public abstract class NicheInputBase {
    private static final String NAME_CHECK_REGEX =  "[\\p{IsAlphabetic}\\p{IsDigit} '&\\(\\)\\-:;\",]+";

    @NotNull
    @Size(min = ChannelConsumer.MIN_NAME_LENGTH, max = ChannelConsumer.MAX_NAME_LENGTH, message = "{field.minMaxSize}")
    @Pattern(regexp = NAME_CHECK_REGEX, message = "{NicheInputDTO.name.Pattern}")
    private final String name;

    @NotNull
    @Size(min = ChannelConsumer.MIN_DESCRIPTION_LENGTH, max = ChannelConsumer.MAX_DESCRIPTION_LENGTH, message = "{field.minMaxSize}")
    private final String description;

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
