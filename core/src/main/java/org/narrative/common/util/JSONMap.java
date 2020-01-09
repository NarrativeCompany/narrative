package org.narrative.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.network.core.propertyset.base.services.PropertiesPropertyMap;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 6/24/12
 * Time: 3:01 PM
 * User: jonmark
 */
public class JSONMap {
    private static final NetworkLogger logger = new NetworkLogger(JSONMap.class);

    private Map<String, Object> map;

    public JSONMap(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> getInternalMap() {
        return map;
    }

    public static JSONMap getJsonMap(String json) {
        // bl: if there is no JSON, then there is no map to load. this will avoid an NPE in mapper.readValue().
        if (isEmpty(json)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> map = mapper.readValue(json, Map.class);
            if (isEmptyOrNull(map)) {
                return null;
            }

            return new JSONMap(map);

        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Encountered unparsable JSON, could be improper call or API change!", e);
            }
        }

        return null;
    }

    public <T> T createPojo(Class<T> pojoClass) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(map, pojoClass);
    }

    public JSONMap getMap(String key) {
        Object value = map.get(key);
        if (value == null || !(value instanceof Map)) {
            return null;
        }

        return new JSONMap((Map<String, Object>) value);
    }

    public <T> List<T> getList(String key) {
        Object value = map.get(key);
        if (value == null || !(value instanceof List)) {
            return null;
        }

        List<?> rawList = (List<?>) value;
        List<T> list = newArrayList(rawList.size());
        for (Object obj : rawList) {
            // bl: if it's an array of Objects, then we just need to add the objects directly to the resulting list.
            if (obj instanceof Map) {
                list.add((T) new JSONMap((Map<String, Object>) obj));
            } else {
                list.add((T) obj);
            }
        }

        return list;
    }

    public String getString(String key) {
        Object value = map.get(key);
        if (value == null || !(value instanceof String)) {
            return null;
        }

        return (String) value;
    }

    public Integer getInteger(String key) {
        Object value = map.get(key);
        if (value == null || !(value instanceof Number)) {
            return null;
        }

        return ((Number) value).intValue();
    }

    public Long getLong(String key) {
        Object value = map.get(key);
        if (value == null || !(value instanceof Number)) {
            return null;
        }

        return ((Number) value).longValue();
    }

    public boolean getBoolean(String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return IPUtil.getBooleanFromString((String) value);
        }

        if (!(value instanceof Boolean)) {
            return false;
        }

        return (Boolean) value;
    }

    // ex: 2013-04-23T16:17:41.484Z
    // per: http://stackoverflow.com/questions/12487125/java-how-do-you-convert-a-utc-timestamp-to-local-time
    private static final SimpleDateFormat UTC_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        UTC_DATETIME_FORMAT.setTimeZone(IPDateUtil.UTC_TIMEZONE);
    }

    public Timestamp getUtcTimestamp(String key) {
        return geTimestamp(key, UTC_DATETIME_FORMAT);
    }

    // ex: 2015-02-09T16:54:46+0000
    // per: http://stackoverflow.com/questions/15433377/how-parse-2013-03-13t2059310000-date-string-to-date
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public Timestamp getTimestamp(String key) {
        return geTimestamp(key, DATETIME_FORMAT);
    }

    private Timestamp geTimestamp(String key, SimpleDateFormat format) {
        Object value = map.get(key);

        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return new Timestamp(getUtilDate((String) value, format).getTime());
        }

        if (!(value instanceof Timestamp)) {
            return null;
        }

        return (Timestamp) value;
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Calendar getCalendar(String key) {
        Object value = map.get(key);

        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getUtilDate((String) value, DATE_FORMAT));
            return calendar;
        }

        if (!(value instanceof Calendar)) {
            return null;
        }

        return (Calendar) value;
    }

    private java.util.Date getUtilDate(String value, SimpleDateFormat format) {
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw UnexpectedError.getRuntimeException(newString("Failed parsing UTC Timestamp: ", value, format.toPattern()), e);
        }
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public <T> T getWrappedInstance(Class<T> cls) {
        // jw: Because the underlying map can have Object values we need to strip it down to just the values that are
        //     strings so that we can properly wrap it using our PropertySetTypeUtil.  This may skill other objects like
        //     Long, Integer or Boolean, which we may want to add support for at some point.  
        Map<String, String> stringMap = newHashMap();
        for (String key : map.keySet()) {
            String value = getString(key);
            if (value != null) {
                stringMap.put(key, value);
            }
        }
        return PropertySetTypeUtil.getPropertyWrapper(cls, new PropertiesPropertyMap(stringMap));
    }

    public <T> T getWrappedInstance(Class<T> cls, Map<String, String> extraValues) {
        // jw: Because the underlying map can have Object values we need to strip it down to just the values that are
        //     strings so that we can properly wrap it using our PropertySetTypeUtil.  This may skill other objects like
        //     Long, Integer or Boolean, which we may want to add support for at some point.
        Map<String, String> stringMap = newHashMap();
        for (String key : map.keySet()) {
            String value = getString(key);
            if (value != null) {
                stringMap.put(key, value);
            }
        }
        if (extraValues != null) {
            stringMap.putAll(extraValues);
        }

        return PropertySetTypeUtil.getPropertyWrapper(cls, new PropertiesPropertyMap(stringMap));
    }
}
