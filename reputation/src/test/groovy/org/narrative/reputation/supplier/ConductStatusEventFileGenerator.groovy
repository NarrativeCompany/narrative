package org.narrative.reputation.supplier

import org.narrative.shared.event.reputation.ConductEventType
import org.narrative.shared.event.reputation.ConductStatusEvent

class ConductStatusEventFileGenerator implements Iterator<ConductStatusEvent>{

    List<String> lines
    private int counter

    ConductStatusEventFileGenerator(String filePath) {
        lines = new File(filePath).readLines()
        // Skip the first, definition, line
        counter++
    }

    @Override
    boolean hasNext() {
        return counter < lines.size()
    }

    @Override
    ConductStatusEvent next() {
        def (userOid, conductEventTypeStr) = lines[counter++].tokenize(",")
        ConductEventType conductEventType = conductEventTypeStr as ConductEventType;
        def e = ConductStatusEvent.builder()
                .userOid((userOid as Integer))
                .conductEventType(conductEventType)
                .build()
        return e
    }
}
