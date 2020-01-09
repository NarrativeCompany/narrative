package org.narrative.network.customizations.narrative;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-08
 * Time: 13:28
 *
 * @author jonmark
 */
@Getter
@FieldNameConstants
@EqualsAndHashCode
public class NrveValueDetail implements Serializable {
    private static final long serialVersionUID = 8952205647801946300L;

    private final NrveValue nrve;
    private final NrveValue nrveRounded;
    // bl: this has to be a string so that it supports leading decimals!
    private final String nrveDecimal;

    public NrveValueDetail(NrveValue nrve) {
        this.nrve = nrve;
        // bl: just round every value down to the nearest NRVE value
        this.nrveRounded = nrve.roundToNearestNrve();
        // bl: we always want the decimal value to be positive, so use absolute value here.
        // bl: we don't need trailing zeros, either, so strip them.
        String decimalStr = nrve.getValue().divideAndRemainder(BigDecimal.ONE)[1].abs().stripTrailingZeros().toPlainString();

        // bl: the string should either be "0" or should start with "0.", which we'll need to strip that off the beginning.
        if(decimalStr.startsWith("0.")) {
            this.nrveDecimal = decimalStr.substring(2);
        } else {
            this.nrveDecimal = decimalStr;
            assert "0".equals(nrveDecimal) : "Found a non-zero, non-decimal value! Should never be possible! nrve/" + nrve + " nrveDecimal/" + nrveDecimal;
        }
        assert !isEmpty(this.nrveDecimal) : "Should never get an empty nrveDecimal! nrve/" + nrve + " nrveDecimal/" + this.nrveDecimal;
        assert !this.nrveDecimal.contains(".") : "Should never get a nrveDecimal with a period! nrve/" + nrve + " nrveDecimal/" + this.nrveDecimal;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
