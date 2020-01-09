package org.narrative.common.persistence

import spock.lang.Specification

class OIDSpec extends Specification {
    def "valueOfNonNumeric does not throw NullPointerException when passed a null"() {
        expect:
            OID.valueOfNonNumeric(null) == null
    }

    def "valueOfNonNumeric returns an OID when passed a number"() {
        given:
            OID newOid = OID.valueOfNonNumeric("123")

        expect:
            newOid.value == 123
    }

    def "valueOfNonNumeric returns an value hash when passed a non-number"() {
        given:
            OID newOid = OID.valueOfNonNumeric("tester")

        expect:
            newOid.value == "tester".hashCode()
    }
}
