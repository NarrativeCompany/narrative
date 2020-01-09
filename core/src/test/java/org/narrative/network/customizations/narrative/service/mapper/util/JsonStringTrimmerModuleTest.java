package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Tested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonStringTrimmerModuleTest {

    @Tested
    JsonStringTrimmerModule jsonStringTrimmerModule;

    @Test
    void deserialize_spacesInValue_removesSpacesAndZeroWidthSpaces() throws IOException {
        final String jsonString = "{ \"val\":\"  123  ​  \"}";

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jsonStringTrimmerModule);

        final StringValueType actualObj = mapper.readValue(jsonString, StringValueType.class);

        assertEquals("123", actualObj.val);
    }

    @Test
    void deserialize_noSpacesInValue_returnsString() throws IOException {
        final String jsonString = "{ \"val\":\"123\"}";

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jsonStringTrimmerModule);

        final StringValueType actualObj = mapper.readValue(jsonString, StringValueType.class);

        assertEquals("123", actualObj.val);
    }

    @Test
    void deserialize_spacesInPassword_leavesSpacesIntact() throws IOException {
        final String jsonString = "{ \"password\":\"  123  \"}";

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jsonStringTrimmerModule);

        final PasswordValueType actualObj = mapper.readValue(jsonString, PasswordValueType.class);

        assertEquals("  123  ", actualObj.password);
    }

    static class StringValueType {
        public String val;
    }

    static class PasswordValueType {
        public String password;
    }

}