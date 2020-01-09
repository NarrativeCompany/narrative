package org.narrative.shared.jpa.type.converter;

import org.narrative.shared.jpa.type.IntEnum;

import javax.persistence.AttributeConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseIntEnumTypeConverter<E extends Enum, T extends IntEnum<E>> implements AttributeConverter<T, Integer> {
    private final Map<Integer, T> ordinalToEnumMap;

    BaseIntEnumTypeConverter(Class<T> enumClass) {
        Map<Integer, T> intEnumMap = new HashMap<>();
        for (T enumVal: enumClass.getEnumConstants()) {
            intEnumMap.put(enumVal.getId(), enumVal);
        }
        ordinalToEnumMap = Collections.unmodifiableMap(intEnumMap);
    }

    @Override
    public Integer convertToDatabaseColumn(T attribute) {
        return attribute.getId();
    }

    @Override
    public T convertToEntityAttribute(Integer dbData) {
        return ordinalToEnumMap.get(dbData);
    }
}
