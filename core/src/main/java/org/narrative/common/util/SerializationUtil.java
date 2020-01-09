package org.narrative.common.util;

import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Oct 24, 2006
 * Time: 1:27:11 PM
 *
 * @author Brian
 */
public class SerializationUtil {
    private static final String PROPERTY_SEPARATOR = ",";
    private static final String MAP_KEY_VALUE_SEPARATOR = ":";
    private static final String AMPERSAND = "&";

    private static final String AMPERSAND_REPLACEMENT = "&a;";
    private static final String PROPERTY_SEPARATOR_REPLACEMENT = "&s;";
    private static final String MAP_KEY_VALUE_SEPARATOR_REPLACEMENT = "&kv;";

    private static final EncodeDecodeHelper ENCODE_DECODE_PROPERTY = new EncodeDecodeHelper(AMPERSAND, AMPERSAND_REPLACEMENT, Collections.singletonMap(PROPERTY_SEPARATOR, PROPERTY_SEPARATOR_REPLACEMENT));
    private static final EncodeDecodeHelper ENCODE_DECODE_MAP_KEY_VALUE;

    static {
        Map<String, String> map = newLinkedHashMap();
        map.put(PROPERTY_SEPARATOR, PROPERTY_SEPARATOR_REPLACEMENT);
        map.put(MAP_KEY_VALUE_SEPARATOR, MAP_KEY_VALUE_SEPARATOR_REPLACEMENT);
        ENCODE_DECODE_MAP_KEY_VALUE = new EncodeDecodeHelper(AMPERSAND, AMPERSAND_REPLACEMENT, map);
    }

    private static final HashMap<String, Locale> cachedLocales = new HashMap<String, Locale>();

    static {
        cachedLocales.put(Locale.getDefault().toString(), Locale.getDefault());

        /** if/when we need to cache all the locales.

         for (Locale locale : Locale.getAvailableLocales()) {
         cachedLocales.put(locale.toString(),locale);
         }
         */

    }

    public static String getPropertyValue(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> cls = object.getClass();
        // if this is an array or a collection, then serialize it accordingly.
        if (cls.isArray()) {
            return serializeArray(object);
        }

        if (Collection.class.isAssignableFrom(cls)) {
            return serializeCollection((Collection) object);
        }

        if (Map.class.isAssignableFrom(cls)) {
            return serializeMap((Map) object);
        }

        if (TimeZone.class.isAssignableFrom(cls)) {
            return ((TimeZone) object).getID();
        }

        if (IntegerEnum.class.isAssignableFrom(cls)) {
            return Integer.toString(((IntegerEnum) object).getId());
        }

        if (StringEnum.class.isAssignableFrom(cls)) {
            return ((StringEnum) object).getIdStr();
        }

        // jw: finally, if we could not identify it by anything above, let's trust its toString is setup to work as we intend.
        return object.toString();
    }

    private static void assertNotArrayCollectionMap(Object object) {
        if (object == null) {
            return;
        }
        // bl: now that we are using getPropertyValue above to get the array/collection values and map key/values,
        // we need to enforce that we don't have a nested array/collection/map inside of another array/collection/map.
        // we don't do that at all currently, so if we want to support it, we will need to reconsider our encoding/decoding
        // strategy in ENCODE_DECODE_PROPERTY and ENCODE_DECODE_MAP_KEY_VALUE
        Class<?> cls = object.getClass();
        assert !cls.isArray() : "Don't support nested arrays via SerializationUtil!";
        assert !Collection.class.isAssignableFrom(cls) : "Don't support nested Collections via SerializationUtil!";
        assert !Map.class.isAssignableFrom(cls) : "Don't support nested Maps via SerializationUtil!";
    }

    public static Object getReturnValue(Object object, Method getterMethod, String propertyValue) throws Throwable {
        if (propertyValue == null) {
            return null;
        }
        Class<?> returnType = IPBeanUtil.extractReturnType(object.getClass(), getterMethod);
        // bl: if we couldn't extract the proper enum return type, then check the interfaces
        // bl: commenting out for now, since Struts fails on this stuff, too. means we need to actually
        // specify the specific types directly in our interfaces.
        /*if(Enum.class.equals(returnType)) {
            returnType = extractErasedEnumReturnType(object, getterMethod);
            if(Enum.class.equals(returnType)) {
                throw UnexpectedError.getRuntimeException("Could not determine return type! Generic param resolved to Enum? returnType/" + returnType);
            }
        }*/
        if (returnType.isArray()) {
            return deserializeArray(propertyValue, returnType);
        }

        if (Collection.class.isAssignableFrom(returnType)) {
            Class<? extends Collection> collectionReturnType = (Class<? extends Collection>) returnType;
            Class<?> collectionObjectClass = IPBeanUtil.extractReturnTypeCollectionType(getterMethod);
            return deserializeCollection(propertyValue, collectionReturnType, collectionObjectClass);
        }

        if (Map.class.isAssignableFrom(returnType)) {
            assert Map.class.equals(returnType) : "PropertySetTypeUtil only supports java.util.Map (and not subclasses thereof)!  Supplied, invalid return type: " + returnType.getName();
            Class<?> mapKeyObjectClass = IPBeanUtil.extractReturnTypeMapKeyType(getterMethod);
            Class<?> mapValueObjectClass = IPBeanUtil.extractReturnTypeMapValueType(getterMethod);
            return deserializeMap(propertyValue, mapKeyObjectClass, mapValueObjectClass);
        }

        return getReturnValueObject(returnType, propertyValue);
    }

    /**
     * bl: nasty, nasty method.  necessary in order to extract the proper generics information from interface
     * implementations wrapped by cglib (which effectively lose generics parameterization).
     *
     * @param object       the object containing the method whose return type is being identified
     * @param getterMethod the method to identify the return type for
     * @return the extracted return type for the given object and method.
     * @throws Throwable
     */
    /*private static Class<?> extractErasedEnumReturnType(Object object, Method getterMethod) throws Throwable {
        Class<?> interfaceClass = null;
        for(Class<?> cls : object.getClass().getInterfaces()) {
            try {
                cls.getMethod(getterMethod.getName());
                interfaceClass = cls;
                break;
            } catch(Exception e) {
                // ignore
            }
        }
        if(interfaceClass!=null) {
            Type genericReturnType = null;
            outer: for(Type type : interfaceClass.getGenericInterfaces()) {
                if(type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)type;
                    Type rawType = parameterizedType.getRawType();
                    if(rawType instanceof Class) {
                        Class rawTypeClass = (Class)rawType;
                        if(rawTypeClass.isAssignableFrom(interfaceClass)) {
                            Type methodGenericReturnType = rawTypeClass.getMethod(getterMethod.getName()).getGenericReturnType();
                            int i=0;
                            for(TypeVariable typeVariable : rawTypeClass.getTypeParameters()) {
                                if(methodGenericReturnType.equals(typeVariable)) {
                                    genericReturnType = parameterizedType.getActualTypeArguments()[i];
                                    break outer;
                                }
                                i++;
                            }
                        }
                    }
                }
            }
            return IPBeanUtil.extractTypeClass(object.getClass(), genericReturnType, "SerializationUtil.parameterizedMethodExtraction");
        }
        return Enum.class;
    }*/
    public static String serializeArray(Object array) {
        if (array == null) {
            return null;
        }
        assert array.getClass().isArray() : "Should only serialize actual arrays!";
        StringBuilder sb = new StringBuilder();
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(PROPERTY_SEPARATOR);
            }
            Object o = Array.get(array, i);
            assertNotArrayCollectionMap(o);
            sb.append(o == null ? null : ENCODE_DECODE_PROPERTY.encode(getPropertyValue(o)));
        }
        return sb.toString();
    }

    public static Object deserializeArray(String serializedArray, Class arrayType) {
        if (serializedArray == null) {
            return null;
        }
        assert arrayType.isArray() : "Should only deserialize actual arrays!";
        Class componentType = arrayType.getComponentType();
        // empty string?  return an empty array.
        if ("".equals(serializedArray)) {
            return Array.newInstance(componentType, 0);
        }
        StringTokenizer st = new StringTokenizer(serializedArray, PROPERTY_SEPARATOR);
        int countTokens = st.countTokens();
        Object ret = Array.newInstance(componentType, countTokens);
        for (int i = 0; i < countTokens; i++) {
            Array.set(ret, i, getReturnValueObject(componentType, ENCODE_DECODE_PROPERTY.decode(st.nextToken()), true));
        }
        return ret;
    }

    public static String serializeCollection(Collection col) {
        if (col == null) {
            return null;
        }
        Iterator iter = col.iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            Object o = iter.next();
            assertNotArrayCollectionMap(o);
            sb.append(o == null ? null : ENCODE_DECODE_PROPERTY.encode(getPropertyValue(o)));
            if (iter.hasNext()) {
                sb.append(PROPERTY_SEPARATOR);
            }
        }
        return sb.toString();
    }

    public static <T, C extends Collection<T>> C deserializeCollection(String serializedCollection, Class<C> collectionType, Class<T> collectionObjectClass) {
        if (serializedCollection == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(serializedCollection, PROPERTY_SEPARATOR);
        int countTokens = st.countTokens();
        Collection<T> ret;
        if (Collection.class.equals(collectionType) || Set.class.equals(collectionType)) {
            ret = new LinkedHashSet<T>();
        } else if (List.class.equals(collectionType)) {
            ret = new ArrayList<T>(countTokens);
        } else {
            assert false : "SerializationUtil only supports java.util.Collection, java.util.Set, and java.util.List for collections! Supplied, invalid return type: " + collectionType.getName();
            return null;
        }
        while (st.hasMoreTokens()) {
            ret.add((T) getReturnValueObject(collectionObjectClass, ENCODE_DECODE_PROPERTY.decode(st.nextToken()), true));
        }
        return (C) ret;
    }

    public static <K, V> String serializeMap(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            assertNotArrayCollectionMap(key);
            sb.append(key == null ? null : ENCODE_DECODE_MAP_KEY_VALUE.encode(getPropertyValue(key)));
            sb.append(MAP_KEY_VALUE_SEPARATOR);
            assertNotArrayCollectionMap(value);
            sb.append(value == null ? null : ENCODE_DECODE_MAP_KEY_VALUE.encode(getPropertyValue(value)));
            if (iter.hasNext()) {
                sb.append(PROPERTY_SEPARATOR);
            }
        }
        return sb.toString();
    }

    public static <K, V> Map<K, V> deserializeMap(String serializedMap, Class<K> mapKeyObjectClass, Class<V> mapValueObjectClass) {
        if (serializedMap == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(serializedMap, PROPERTY_SEPARATOR);
        Map<K, V> ret = new LinkedHashMap<K, V>();
        while (st.hasMoreTokens()) {
            String keyAndValueStr = st.nextToken();
            StringTokenizer keyAndValue = new StringTokenizer(keyAndValueStr, MAP_KEY_VALUE_SEPARATOR);

            // jw: String.split, and StringTokenizer do not return empty strings so if the actual String we are tokenizing
            //     has a empty value (ex: "KEY:"), then we will only have a token count of 1 (for the key).  We should always
            //     have at least a key so I am leaving that much of a assertion.
            boolean includeValue = keyAndValue.countTokens() == 2;
            assert keyAndValue.countTokens() == 1 || includeValue : "Found a keyAndValue pair that didn't have at least one token, and no more than 2!  Means that there is an improper use of a Map! kv/" + keyAndValueStr + ". For Maps used by SerializationUtil, you can't have a comma ',' or a colon ':' in either the key or value.";
            // jw: the first value is always the key
            String rawKey = ENCODE_DECODE_MAP_KEY_VALUE.decode(keyAndValue.nextToken());
            K key = (K) getReturnValueObject(mapKeyObjectClass, rawKey, true);

            // jw: this is where things get a bit trickier, if we do not have a value from the StringTokenizer then just use empty string.
            V value = (V) getReturnValueObject(mapValueObjectClass, includeValue ? ENCODE_DECODE_MAP_KEY_VALUE.decode(keyAndValue.nextToken()) : "", true);
            // jw: lets only store the value if we have a key, or the rawKey was empty which explains why we did not parse
            //     a key value, or its a enum, which if we cannot find a constant for we will return null so dont store that.
            if (key != null || isEmpty(rawKey) || !Enum.class.isAssignableFrom(mapKeyObjectClass)) {
                ret.put(key, value);
            }
        }
        return ret;
    }

    public static Object getReturnValueObject(Class<?> returnType, String propertyValue) {
        return getReturnValueObject(returnType, propertyValue, false);
    }

    private static Object getReturnValueObject(Class<?> returnType, String propertyValue, boolean convertNullStringToNull) {
        if (propertyValue == null) {
            return null;
        }
        // going to convert "null" to a null reference for collections, maps, and arrays
        if (convertNullStringToNull && "null".equals(propertyValue)) {
            return null;
        }
        // special handling for primitives to get the Object type.
        if (returnType.isPrimitive()) {
            // todo: please tell me there is a better way to handle primitives
            // todo: should use Integer.valueOf, Boolean.valueOf, etc. instead of the String constructor.
            if (Integer.TYPE.equals(returnType)) {
                returnType = Integer.class;
            } else if (Boolean.TYPE.equals(returnType)) {
                returnType = Boolean.class;
            } else if (Double.TYPE.equals(returnType)) {
                returnType = Double.class;
            } else if (Float.TYPE.equals(returnType)) {
                returnType = Float.class;
            } else if (Byte.TYPE.equals(returnType)) {
                returnType = Byte.class;
            } else if (Character.TYPE.equals(returnType)) {
                returnType = Character.class;
            } else if (Short.TYPE.equals(returnType)) {
                returnType = Short.class;
            } else if (Long.TYPE.equals(returnType)) {
                returnType = Long.class;
            }
        }
        // special handling for Timestamp since it doesn't have a constructor that takes a String.
        if (returnType.equals(Timestamp.class)) {
            return new Timestamp(Timestamp.valueOf(propertyValue).getTime());
        }
        if (returnType.equals(TimeZone.class)) {
            return TimeZone.getTimeZone(propertyValue);
        }
        if (Enum.class.isAssignableFrom(returnType)) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) returnType;
            // bl: first try IntegerEnum
            if (IntegerEnum.class.isAssignableFrom(enumClass)) {
                try {
                    int id = Integer.parseInt(propertyValue);
                    IntegerEnum retVal = EnumRegistry.getForId((Class<? extends IntegerEnum>) enumClass, id, false);
                    if (retVal != null) {
                        return retVal;
                    }
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
            // bl: then try StringEnum
            if (StringEnum.class.isAssignableFrom(enumClass)) {
                StringEnum retVal = EnumRegistry.getForId((Class<? extends StringEnum>) enumClass, propertyValue, false);
                if (retVal != null) {
                    return retVal;
                }
            }
            // bl: if all else fails, then try to just get by the Enum name()
            try {
                return Enum.valueOf(enumClass, propertyValue);
            } catch (IllegalArgumentException iae) {
                // jw: Lets attempt to get the enum constant by upper casing the propertyValue, if that fails lets just
                //     return null so that serialized instances of removed constants will no longer cause errors
                //     previously we used to just do the Enum.valueOf call without this addition try/catch, which caused
                //     removed constants to break whatever code they were being deserialized in (usually Settings).
                try {
                    return Enum.valueOf(enumClass, propertyValue.toUpperCase());
                } catch (IllegalArgumentException iae2) {
                    return null;
                }
            }
        }
        if (returnType.equals(Locale.class)) {
            return getCachedLocale(propertyValue);
        }
        try {
            Constructor constructor = returnType.getConstructor(String.class);
            return constructor.newInstance(propertyValue);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Failed getting String constructor for object " + returnType, t, true);
        }
    }

    private static Locale getCachedLocale(String propertyValue) {

        Locale output = cachedLocales.get(propertyValue);
        if (output == null) {
            for (Map.Entry<String, Locale> stringLocaleEntry : cachedLocales.entrySet()) {
                if (stringLocaleEntry.getKey().startsWith(propertyValue)) {
                    output = stringLocaleEntry.getValue();
                    break;
                }
            }
        }

        cachedLocales.put(propertyValue, output);

        return output;
    }
}
