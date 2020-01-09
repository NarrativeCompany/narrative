package org.narrative.network.customizations.narrative;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.narrative.network.customizations.narrative.service.mapper.util.UsdValueSerializer;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Date: 10/23/18
 * Time: 1:14 PM
 *
 * @author jonmark
 */
@EqualsAndHashCode
@JsonSerialize(using = UsdValueSerializer.class)
public class UsdValue implements Serializable {
    private static final long serialVersionUID = 2179602333444859911L;

    private final BigDecimal value;

    public UsdValue(BigDecimal value) {
        this.value = value;
    }

    public String getFormattedAsUsd() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }

    public static UsdValue valueOf(BigDecimal value) {
        if (value==null) {
            return null;
        }

        return new UsdValue(value);
    }
}
