package org.narrative.network.customizations.narrative;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Date: 2019-06-03
 * Time: 15:40
 *
 * @author brian
 */
@Getter
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class NrveUsdValue extends NrveValueDetail implements Serializable {
    private static final long serialVersionUID = 5726372671444756435L;

    private final UsdValue usd;

    /**
     * construct a NrveUsdValue based on the current market value of NRVE
     * @param nrve the NRVE amount
     */
    public NrveUsdValue(NrveValue nrve) {
        this(nrve, GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice());
    }

    /**
     * this constructor is here purely for testing so we can deserialize responses from JSON.
     */
    @Builder
    public NrveUsdValue(@JsonProperty(Fields.nrve) NrveValue nrve, @JsonProperty(Fields.usd) String nrveUsdPrice) {
        // bl: strip off the leading $ from the UsdValue serialization
        this(nrve, new BigDecimal(nrveUsdPrice.substring(1)));
    }

    /**
     * construct a NrveUsdValue based on a supplied value of NRVE
     * @param nrve the NRVE amount
     * @param nrveUsdPrice the price of NRVE
     */
    public NrveUsdValue(NrveValue nrve, BigDecimal nrveUsdPrice) {
        super(nrve);

        BigDecimal nrveUsdAmount = nrveUsdPrice.multiply(nrve.getValue()).setScale(2, RoundingMode.CEILING);
        this.usd = new UsdValue(nrveUsdAmount);
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields extends NrveValueDetail.Fields {}
}
