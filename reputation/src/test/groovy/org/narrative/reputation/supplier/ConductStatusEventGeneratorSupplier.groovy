package org.narrative.reputation.supplier

import org.narrative.shared.event.reputation.ConductEventType
import org.narrative.shared.event.reputation.ConductStatusEvent
import org.testcontainers.shaded.org.apache.commons.lang.math.RandomUtils

class ConductStatusEventGeneratorSupplier implements Iterator<ConductStatusEvent> {
    private static final ConductEventType[] eventTypes = ConductEventType.values();

    private int counter
    private int size

    ConductStatusEventGeneratorSupplier(int numberOfEvents){
        size = numberOfEvents
    }

    @Override
    boolean hasNext() {
        counter<size
    }

    @Override
    ConductStatusEvent next() {
        counter++

        int idx = RandomUtils.nextInt(ConductEventType.getEnumConstants().length);
        ConductEventType cet = eventTypes[idx];

        return ConductStatusEvent.builder()
                .userOid(RandomUtils.nextInt(10) + 1)
                .conductEventType(cet)
                .build()

    }
}
