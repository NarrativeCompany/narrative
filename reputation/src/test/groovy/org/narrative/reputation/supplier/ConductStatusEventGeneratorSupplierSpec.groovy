package org.narrative.reputation.supplier

import spock.lang.Specification

class ConductStatusEventGeneratorSupplierSpec extends Specification {

    def "test get"() {
        expect:
            conductStatusEvent.conductEventType.getSeverity() <= 3
            conductStatusEvent.conductEventType.getSeverity() >= 1
        where:
            // Test 100 events
            conductStatusEvent << new ConductStatusEventGeneratorSupplier(100)

    }
}
