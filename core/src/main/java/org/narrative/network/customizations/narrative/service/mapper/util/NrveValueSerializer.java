package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.narrative.network.customizations.narrative.NrveValue;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 8/10/18
 * Time: 8:40 AM
 *
 * @author brian
 */
public class NrveValueSerializer extends StdScalarSerializer<NrveValue> {
    public NrveValueSerializer() {
        super(NrveValue.class);
    }

    @Override
    public void serialize(NrveValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.getFormattedWithEightDecimals());
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("string", true);
    }
}
