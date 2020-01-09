package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.narrative.network.customizations.narrative.UsdValue;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 10/23/18
 * Time: 1:24 PM
 *
 * @author jonmark
 */
public class UsdValueSerializer extends StdScalarSerializer<UsdValue> {
    public UsdValueSerializer() {
        super(UsdValue.class);
    }

    @Override
    public void serialize(UsdValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.getFormattedAsUsd());
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("string", true);
    }
}
