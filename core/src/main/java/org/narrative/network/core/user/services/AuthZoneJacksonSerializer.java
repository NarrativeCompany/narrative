package org.narrative.network.core.user.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.narrative.network.core.user.AuthZone;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 6/28/12
 * Time: 3:26 PM
 *
 * @author brian
 */
public class AuthZoneJacksonSerializer extends StdScalarSerializer<AuthZone> {
    public AuthZoneJacksonSerializer() {
        super(AuthZone.class);
    }

    @Override
    public void serialize(AuthZone value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeNumber(value.getOid().getValue());
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("number", true);
    }
}
