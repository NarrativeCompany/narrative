package org.narrative.network.customizations.narrative.service.mapper.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/26/18
 * Time: 10:39 AM
 *
 * @author brian
 */
public class TimeZoneDeserializer extends StdScalarDeserializer<TimeZone> {
    public TimeZoneDeserializer() {
        super(TimeZone.class);
    }

    @Override
    public TimeZone deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String timeZoneId = p.getValueAsString();
        if(StringUtils.isEmpty(timeZoneId)) {
            return null;
        }
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        // bl: only return the TimeZone if its ID actually matches the supplied value.
        // this is needed since getTimeZone will return GMT by default if the ID is unrecognized.
        // we don't want that behavior and instead want to ignore unknown values.
        if(isEqual(timeZone.getID(), timeZoneId)) {
            return timeZone;
        }
        return null;
    }
}
