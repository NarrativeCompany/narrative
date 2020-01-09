package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.neo.services.CoinMarketCapApiRequests;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.Instant;

import static org.narrative.common.util.IPUtil.*;

/**
 * Date: 2019-07-02
 * Time: 09:19
 *
 * @author jonmark
 */
@Data
@FieldNameConstants
public class NrveUsdPriceFields {
    private static final String SECURITY_TOKEN_SEED = NrveUsdPriceFields.class.getSimpleName()+"-NrveToUsdTokenSeedValue-bolivar-sing-herdsmen-snobbery-clothing-soviet-didst-twelve-funnel-dolt";

    public static final String NRVE_USD_PRICE_COLUMN_DEFINITION = "decimal(19," + CoinMarketCapApiRequests.MAX_PRICE_DECIMAL_DIGITS + ")";

    private final BigDecimal nrveUsdPrice;
    private final Instant expirationDatetime;
    private final String securityToken;

    @JsonIgnore
    public boolean isValid() {
        return isValid(this);
    }

    public BigDecimal convertToBigDecimal(NrveValue nrveValue) {
        return convertToBigDecimal(nrveUsdPrice, nrveValue);
    }

    public UsdValue convert(NrveValue nrveValue) {
        return convert(nrveUsdPrice, nrveValue);
    }

    public static boolean isValid(NrveUsdPriceFields fields) {
        if (fields.nrveUsdPrice ==null || fields.expirationDatetime==null || fields.securityToken==null) {
            return false;
        }

        // bl: make sure the expiration date hasn't passed!
        if(!fields.expirationDatetime.isAfter(Instant.now())) {
            return false;
        }

        return isEqual(
                generateSecurityToken(fields.nrveUsdPrice, fields.expirationDatetime),
                fields.securityToken
        );
    }

    public static String generateSecurityToken(BigDecimal nrveUsdPrice, Instant expirationDatetime) {
        assert nrveUsdPrice != null : "We should always have a nrveUsdPrice!";
        assert expirationDatetime != null : "We should always have a expirationDatetime!";

        return IPStringUtil.getMD5DigestFromObjects(
                SECURITY_TOKEN_SEED,
                nrveUsdPrice,
                expirationDatetime
        );
    }

    private static BigDecimal convertToBigDecimal(BigDecimal nrveUsdPrice, NrveValue nrveValue) {
        return nrveValue.getValue().multiply(nrveUsdPrice);
    }

    public static UsdValue convert(BigDecimal nrveUsdPrice, NrveValue nrveValue) {
        return new UsdValue(convertToBigDecimal(nrveUsdPrice, nrveValue));
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
