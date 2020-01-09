package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * Date: 10/18/18
 * Time: 9:38 AM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class CreateNicheRequest extends NicheInputBase {
    @AssertTrue(message = "{CreateNicheInputDTO.assertChecked.AssertTrue}")
    private final boolean assertChecked;
    @AssertTrue(message = "{CreateNicheInputDTO.agreeChecked.AssertTrue}")
    private final boolean agreeChecked;

    @Builder
    public CreateNicheRequest(@NotNull String name, @NotNull String description, boolean assertChecked, boolean agreeChecked) {
        super(name, description);
        this.assertChecked = assertChecked;
        this.agreeChecked = agreeChecked;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends NicheInputBase.Fields {}
}
