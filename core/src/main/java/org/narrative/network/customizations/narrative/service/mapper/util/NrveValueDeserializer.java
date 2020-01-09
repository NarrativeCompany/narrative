package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.narrative.network.customizations.narrative.NrveValue;

import java.io.IOException;
import java.math.BigDecimal;

public class NrveValueDeserializer extends StdScalarDeserializer<NrveValue> {
    public NrveValueDeserializer() {
        super(NrveValue.class);
    }

    @Override
    public NrveValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // jw: we are serializing NRVE into its US localized format, ex: 1,234.567
        String formattedValue = p.getValueAsString();
        if (formattedValue==null) {
            return null;
        }

        // jw: since the only character that wont't parse is the comma, let's strip those out.
        return new NrveValue(new BigDecimal(formattedValue.replace(",", "")));
    }
}