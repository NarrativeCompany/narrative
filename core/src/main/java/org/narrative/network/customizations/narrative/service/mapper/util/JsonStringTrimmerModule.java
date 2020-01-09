package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.narrative.common.util.IPStringUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.narrative.network.core.user.PasswordFields.*;

/**
 * A Jackson SimpleModule bean to trim whitespace and Zero Width Spaces in order to deserialize String fields
 */
@Component
public class JsonStringTrimmerModule extends SimpleModule {
    private static final long serialVersionUID = -5273671164300731381L;

    /**
     * Instantiates a new Json string trimmer module.
     */
    public JsonStringTrimmerModule() {
        addDeserializer(String.class, new StringDeserializer() {
            private static final long serialVersionUID = 4513301883444611922L;

            @Override
            public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                String deserializedString = super.deserialize(jsonParser, ctx);

                if (!jsonParser.getCurrentName().toLowerCase().contains(PASSWORD_PARAM)) {
                    // It's not a password doe remove zero width spaces and trim
                    deserializedString = IPStringUtil.removeZeroWidthSpaces(deserializedString);

                    if (deserializedString != null) {
                        deserializedString = deserializedString.trim();
                    }
                }
                return deserializedString;
            }
        });
    }
}
