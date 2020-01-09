package org.narrative.network.customizations.narrative.controller.postbody.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.NrveUsdPriceInput;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Date: 2019-07-02
 * Time: 09:28
 *
 * @author jonmark
 */
public class NrveUsdPriceInputDTO extends NrveUsdPriceInput {
    public NrveUsdPriceInputDTO(
            @JsonProperty(Fields.nrveUsdPrice) BigDecimal nrveUsdPrice,
            @JsonProperty(Fields.expirationDatetime) Instant expirationDatetime,
            @JsonProperty(Fields.securityToken) String securityToken
    ) {
        super(nrveUsdPrice, expirationDatetime, securityToken);
    }
}
