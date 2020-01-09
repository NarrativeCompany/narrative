package org.narrative.common.persistence.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.narrative.common.persistence.OID;

import java.io.IOException;

/**
 * Date: 5/23/12
 * Time: 10:48 AM
 *
 * @author brian
 */
public class OIDJacksonDeserializer extends StdScalarDeserializer<OID> {
    public OIDJacksonDeserializer() {
        super(OID.class);
    }

    @Override
    public OID deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return OID.valueOf(jp.getValueAsString());
    }
}
