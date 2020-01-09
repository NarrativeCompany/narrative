package org.narrative.common.web

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId

class BirthdaySpec extends Specification {

    def "Birthday constructor with Date should initialize the Birthday correctly"() {
        given:
            def date = LocalDateTime.of(1972, 01, 01, 22, 10)

        when:
            def birthday = new Birthday(Date.from(date.atZone(ZoneId.of("UTC")).toInstant()))

        then:
            with(birthday) {
                getMonth() == 0
                getDayOfMonth() == 1
                getYear() == 1972
                isValid()
            }
    }

    def "Birthday is initialized setting month then getting year returns correct year"() {
        given:
            def date = LocalDateTime.of(1972, 01, 01, 22, 10)
            def birthday = new Birthday(Date.from(date.atZone(ZoneId.of("UTC")).toInstant()))

        when:
            birthday.setMonth(4)

        then:
            birthday.getYear() == 1972

    }

    def "Birthday is initialized setting month then isValid returns true"() {
        given:
            def date = LocalDateTime.of(1972, 01, 01, 22, 10)
            def birthday = new Birthday(Date.from(date.atZone(ZoneId.of("UTC")).toInstant()))

        when:
            birthday.setMonth(4)

        then:
            birthday.isValid()

    }

}