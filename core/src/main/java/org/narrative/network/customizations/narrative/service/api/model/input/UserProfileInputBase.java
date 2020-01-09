package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.UsernameUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Date: 9/27/18
 * Time: 8:21 AM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
public class UserProfileInputBase {
    @NotNull
    @Size(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH, message = "{field.minMaxSize}")
    private final String displayName;

    @NotNull
    @Size(min = UsernameUtils.MIN_LENGTH, max = UsernameUtils.MAX_LENGTH, message = "{field.minMaxSize}")
    private final String username;

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
