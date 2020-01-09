package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-02-06
 * Time: 14:25
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class NrvePaymentInput implements InvoicePaymentInput {
    @NotEmpty
    private final String neoAddress;

    @Builder
    public NrvePaymentInput(@NotEmpty String neoAddress) {
        this.neoAddress = neoAddress;
    }
}
