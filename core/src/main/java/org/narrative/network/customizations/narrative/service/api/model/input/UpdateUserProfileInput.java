package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 9/27/18
 * Time: 8:22 AM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
public class UpdateUserProfileInput extends UserProfileInputBase {

    @Builder
    public UpdateUserProfileInput(String displayName, String username) {
        super(displayName, username);
    }
}
