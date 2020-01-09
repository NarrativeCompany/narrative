package org.narrative.network.core.user.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.narrative.network.core.user.AuthZone;

import java.io.IOException;

/**
 * Date: 6/28/12
 * Time: 3:26 PM
 *
 * @author brian
 */
public class AuthZoneJacksonDeserializer extends StdScalarDeserializer<AuthZone> {
    public AuthZoneJacksonDeserializer() {
        super(AuthZone.class);
    }

    @Override
    public AuthZone deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return AuthZone.getAuthZone(jp.getLongValue());
    }
}
