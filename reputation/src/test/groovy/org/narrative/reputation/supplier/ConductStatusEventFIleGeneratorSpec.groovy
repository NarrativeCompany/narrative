package org.narrative.reputation.supplier

import spock.lang.Specification

class ConductStatusEventFIleGeneratorSpec extends Specification {

    def "test get"() {
        expect:
            conductStatusEvent.conductEventType.getSeverity() <= 3
            conductStatusEvent.conductEventType.getSeverity() >= 1
        where:
            conductStatusEvent << new ConductStatusEventFileGenerator("src/test/resources/test-data/ConductStatusEvents.csv")

    }
}