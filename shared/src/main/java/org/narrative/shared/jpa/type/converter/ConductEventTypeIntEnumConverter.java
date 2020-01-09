package org.narrative.shared.jpa.type.converter;

import org.narrative.shared.event.reputation.ConductEventType;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class ConductEventTypeIntEnumConverter extends BaseIntEnumTypeConverter<ConductEventType, ConductEventType> {
    public ConductEventTypeIntEnumConverter() {
        super(ConductEventType.class);
    }
}
