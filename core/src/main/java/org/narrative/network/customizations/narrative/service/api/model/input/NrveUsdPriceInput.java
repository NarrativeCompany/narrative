package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceFields;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Date: 2019-07-02
 * Time: 09:16
 *
 * @author jonmark
 */
@Validated
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NrveUsdPriceInput extends NrveUsdPriceFields {
    public NrveUsdPriceInput(BigDecimal nrveUsdPrice, Instant expirationDatetime, String securityToken) {
        super(nrveUsdPrice, expirationDatetime, securityToken);
    }
}
