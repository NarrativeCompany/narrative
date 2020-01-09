package org.narrative.common.util.enums;

import org.narrative.common.util.UnexpectedError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 11, 2009
 * Time: 12:11:40 PM
 *
 * @author brian
 */
public class EnumRegistry {

    private static final Map<Class, Map<Integer, Enum>> INT_ENUM_VALUES = new ConcurrentHashMap<Class, Map<Integer, Enum>>();
    private static final Map<Class, Map<String, Enum>> STRING_ENUM_VALUES = new ConcurrentHashMap<Class, Map<String, Enum>>();

    public static <T extends IntegerEnum> T getForId(Class<T> enumClass, int id) {
        return getForId(enumClass, id, true);
    }

    public static <T extends IntegerEnum> T getForId(Class<T> enumClass, int id, boolean force) {
        Map<Integer, Enum> valuesMap = INT_ENUM_VALUES.get(enumClass);
        if (valuesMap == null) {
            valuesMap = registerIntegerEnumClass(enumClass);
        }
        assert valuesMap != null : "Failed to properly initialize an enum class! enumClass/" + enumClass;
        Enum anEnum = valuesMap.get(id);
        if (force && anEnum == null) {
            throw UnexpectedError.getRuntimeException("Failed lookup of enum by id/" + id + " enumClass/" + enumClass);
        }
        return (T) anEnum;
    }

    public static <T extends StringEnum> T getForId(Class<T> enumClass, String id) {
        return getForId(enumClass, id, true);
    }

    public static <T extends StringEnum> T getForId(Class<T> enumClass, String id, boolean force) {
        Map<String, Enum> valuesMap = STRING_ENUM_VALUES.get(enumClass);
        if (valuesMap == null) {
            valuesMap = registerStringEnumClass(enumClass);
        }
        assert valuesMap != null : "Failed to properly initialize an enum class! enumClass/" + enumClass;
        Enum anEnum = valuesMap.get(id);
        if (force && anEnum == null) {
            throw UnexpectedError.getRuntimeException("Failed lookup of enum by id/" + id + " enumClass/" + enumClass);
        }
        return (T) anEnum;
    }

    public static synchronized void registerEnumClass(Class<? extends Enum> enumClass) {
        if (IntegerEnum.class.isAssignableFrom(enumClass)) {
            registerIntegerEnumClass((Class<? extends IntegerEnum>) enumClass);
        }

        if (StringEnum.class.isAssignableFrom(enumClass)) {
            registerStringEnumClass((Class<? extends StringEnum>) enumClass);
        }
    }

    private static synchronized Map<Integer, Enum> registerIntegerEnumClass(Class<? extends IntegerEnum> enumClass) {
        assert Enum.class.isAssignableFrom(enumClass) : "Should only ever register Enum classes! cls/" + enumClass;
        Map<Integer, Enum> values = INT_ENUM_VALUES.get(enumClass);
        if (values == null) {
            values = newHashMap();
            Enum[] enums = ((Class<? extends Enum>) enumClass).getEnumConstants();
            for (Enum anEnum : enums) {
                assert !values.containsKey(((IntegerEnum) anEnum).getId()) : "Do not support duplicate ID values in IntegerEnums! enum/" + enumClass;
                /*if(anEnum.ordinal()!=((IntegerEnum) anEnum).getId()) {
                    System.out.println("ID/Ordinal mismatch cls/" + enumClass + " value/" + anEnum);
                }*/
                values.put(((IntegerEnum) anEnum).getId(), anEnum);
            }
            INT_ENUM_VALUES.put(enumClass, values);
        }
        return values;
    }

    private static synchronized Map<String, Enum> registerStringEnumClass(Class<? extends StringEnum> enumClass) {
        assert Enum.class.isAssignableFrom(enumClass) : "Should only ever register Enum classes! cls/" + enumClass;
        return registerStringBasedEnumClass((Class<? extends Enum>) enumClass);
    }

    private static synchronized Map<String, Enum> registerStringBasedEnumClass(Class<? extends Enum> enumClass) {
        Map<Class, Map<String, Enum>> map = STRING_ENUM_VALUES;
        Map<String, Enum> values = map.get(enumClass);
        if (values == null) {
            values = newHashMap();
            Enum[] enums = ((Class<? extends Enum>) enumClass).getEnumConstants();
            for (Enum anEnum : enums) {
                String id = ((StringEnum) anEnum).getIdStr();
                assert !values.containsKey(id) : "Do not support duplicate ID values in String-based Enums! enum/" + enumClass + " id/" + id;
                /*if(!anEnum.name().equals(((StringEnum) anEnum).getIdStr())) {
                    System.out.println("ID/NAME mismatch cls/" + enumClass + " value/" + anEnum);
                }*/
                values.put(id, anEnum);
            }
            map.put(enumClass, values);
        }
        return values;
    }

    public static long getBitForIntegerEnum(IntegerEnum integerEnum) {
        return 1L << integerEnum.getId();
    }
}
