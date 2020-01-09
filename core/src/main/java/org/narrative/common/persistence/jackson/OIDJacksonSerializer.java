package org.narrative.common.persistence.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.narrative.common.persistence.OID;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Date: 5/23/12
 * Time: 10:01 AM
 *
 * @author brian
 */
public class OIDJacksonSerializer extends StdScalarSerializer<OID> {
    public OIDJacksonSerializer() {
        super(OID.class);
    }

    @Override
    public void serialize(OID value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(Long.toString(value.getValue()));
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("number", true);
    }
}
