package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-10
 * Time: 13:47
 *
 * @author brian
 */
public class BigDecimalDeserializer extends StdScalarDeserializer<BigDecimal> {
    public BigDecimalDeserializer() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return convertStringToBigDecimal(value);
    }

    public static BigDecimal convertStringToBigDecimal(String value) {
        if(isEmpty(value)) {
            return null;
        }
        DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(DefaultLocale.getDefaultLocale());
        decimalFormat.setParseBigDecimal(true);
        try {
            return (BigDecimal)decimalFormat.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
