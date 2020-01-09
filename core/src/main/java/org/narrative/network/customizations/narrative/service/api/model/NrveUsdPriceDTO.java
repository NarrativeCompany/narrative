package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Date: 2019-07-02
 * Time: 09:21
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NrveUsdPrice")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NrveUsdPriceDTO extends NrveUsdPriceFields {
    @Builder
    public NrveUsdPriceDTO(BigDecimal nrveUsdPrice, Instant expirationDatetime, String securityToken) {
        super(nrveUsdPrice, expirationDatetime, securityToken);
    }

    public static NrveUsdPriceDTO create(BigDecimal nrveUsdPrice, Duration duration) {
        Instant expirationDatetime = Instant.now().plus(duration);
        return builder()
                .nrveUsdPrice(nrveUsdPrice)
                .expirationDatetime(expirationDatetime)
                .securityToken(NrveUsdPriceFields.generateSecurityToken(nrveUsdPrice, expirationDatetime))
                .build();
    }
}
