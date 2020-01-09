package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-07-01
 * Time: 11:12
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString()
@Validated
@FieldNameConstants
public class ValidateUserNeoWalletInput {
    private final String neoAddress;

    @Builder
    public ValidateUserNeoWalletInput(String neoAddress) {
        this.neoAddress = neoAddress;
    }
}
