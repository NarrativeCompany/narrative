package org.narrative.network.customizations.narrative;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.narrative.common.util.GBigDecimal;
import org.narrative.network.customizations.narrative.service.mapper.util.NrveValueDeserializer;
import org.narrative.network.customizations.narrative.service.mapper.util.NrveValueSerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 9:49 PM
 */
@JsonSerialize(using = NrveValueSerializer.class)
@JsonDeserialize(using = NrveValueDeserializer.class)
public class NrveValue implements Serializable {
    private final BigDecimal value;

    public static final BigDecimal NEURONS_PER_NRVE = BigDecimal.valueOf(100000000); // 100,000,000 neurons per nrve

    public static final NrveValue ZERO = new NrveValue(BigDecimal.ZERO);
    public static final NrveValue ONE = new NrveValue(BigDecimal.ONE);

    // jw: because we only have 8 decimal places max on NRVE, our scale is 8
    public static final int SCALE = 8;

    public NrveValue(BigDecimal value) {
        assert value.scale() <= SCALE : "Should never create a NrveValue with a scale greater than 8! All rounding should take place prior.";
        // bl: strip any trailing zeros in the value to normalize
        this.value = value.stripTrailingZeros();
    }

    public NrveValue(Long neurons) {
        this(neurons == null ? BigDecimal.ZERO : BigDecimal.valueOf(neurons).divide(NEURONS_PER_NRVE, SCALE, RoundingMode.UNNECESSARY));
    }

    // jw: this is necessary for SerializationUtil deserialization.
    public NrveValue(String neurons) {
        this(Long.parseLong(neurons));
    }

    public String getFormattedWithEightDecimals() {
        return GBigDecimal.formatBigDecimal(value, RoundingMode.CEILING, 8, true, false);
    }

    public String getFormattedWithoutGroupings() {
        return GBigDecimal.formatBigDecimal(value, RoundingMode.CEILING, 8, false, false);
    }

    public String getFormattedWithSuffix() {
        return wordlet("nrveValue.formatted", getFormattedWithEightDecimals());
    }

    public BigDecimal getValue() {
        return value;
    }

    private transient Long neurons;

    public long toNeurons() {
        if(neurons==null) {
            neurons = value.multiply(NEURONS_PER_NRVE).longValueExact();
        }
        return neurons;
    }

    public NrveValue roundToNearestNrve() {
        // bl: just always round these values down. note that we shouldn't use FLOOR here or else we'll
        // potentially change the amount for negative values. DOWN ensures that we'll always round toward 0.
        return new NrveValue(value.setScale(0, RoundingMode.DOWN));
    }

    public int compareTo(NrveValue to) {
        // jw: something is always greater than nothing
        if (to == null) {
            return 1;
        }

        return value.compareTo(to.value);
    }

    public NrveValue multiply(long by) {
        return new NrveValue(value.multiply(BigDecimal.valueOf(by)));
    }

    public NrveValue multiply(BigDecimal bigDecimal, RoundingMode roundingMode) {
        return new NrveValue(value.multiply(bigDecimal).setScale(SCALE, roundingMode));
    }

    public NrveValue divide(long by, RoundingMode roundingMode) {
        return new NrveValue(value.divide(BigDecimal.valueOf(by), SCALE, roundingMode));
    }

    public NrveValue add(NrveValue nrve) {
        if (nrve == null) {
            return this;
        }

        return new NrveValue(this.value.add(nrve.value));
    }

    public NrveValue subtract(NrveValue nrve) {
        if (nrve == null) {
            return this;
        }

        return new NrveValue(this.value.subtract(nrve.value));
    }

    public NrveValue min(NrveValue nrve) {
        if (nrve == null || nrve.compareTo(this) >= 0) {
            return this;
        }

        return nrve;
    }

    public NrveValue max(NrveValue nrve) {
        if (nrve == null || nrve.compareTo(this) <= 0) {
            return this;
        }

        return nrve;
    }

    public static NrveValue getNrveValueFromUsd(BigDecimal usdAmount, BigDecimal nrveUsdPrice, int scale, RoundingMode roundingMode) {
        assert scale <= SCALE : "Should never specify a scale/"+scale+" that is greater than what is supported by NrveValue/"+NrveValue.SCALE;

        BigDecimal nrveAmount = usdAmount.divide(nrveUsdPrice, scale, roundingMode);

        return new NrveValue(nrveAmount);
    }

    @Override
    public String toString() {
        // bl: this used to output the neuron amount, which is odd. it really should output the true NRVE value,
        // which is much easier to understand and interpret correctly. neurons are really unexpected in a toString()
        // context considering the value being represented here is NRVE.
        return value.toPlainString();
    }

    public boolean equals(NrveValue nrve) {
        if (nrve == null) {
            return false;
        }

        return compareTo(nrve) == 0;
    }

    public boolean equals(BigDecimal value) {
        if (value == null) {
            return false;
        }
        return this.value.compareTo(value) == 0;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof NrveValue) {
            return equals((NrveValue) o);
        }

        return false;
    }

    public int hashCode() {
        return Long.hashCode(toNeurons());
    }
}
