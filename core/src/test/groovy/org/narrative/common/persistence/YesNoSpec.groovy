package org.narrative.common.persistence

import org.narrative.common.util.UnexpectedError
import spock.lang.Specification

class YesNoSpec extends Specification {
    def "valueOf Boolean method throws UnexpectedError and not an NPE"() {
        when:
            Boolean testVal = null
            YesNo.valueOf(testVal as Boolean)

        then:
            thrown(UnexpectedError)

    }

}
