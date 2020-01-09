package org.narrative.network.core.cluster.setup

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class NetworkSetupSpec extends Specification {
    @Shared
    def TEST_KEY = "testKey"

    @Shared
    def TEST_VALUE = "testValue"

    @Shared
    def ANOTHER_TEST_VALUE = "anotherTestValue"

    @Shared
    def CONTEXT_PREFIXED_TEST_KEY = "server.servlet.context-parameters." + TEST_KEY

    @Unroll
    def "#testPropertyKey should be renamed to #renamedPropertyKey"() {
        when:
            def renamedKey = NetworkSetup.renamePropertyKey(testPropertyKey)
        then:
            renamedPropertyKey == renamedKey
        where:
            testPropertyKey << [CONTEXT_PREFIXED_TEST_KEY, TEST_KEY]
            renamedPropertyKey << [TEST_KEY, TEST_KEY]
    }

    def "source properties when merged into target properties should overwrite duplicate property keys"() {
        given:
            def source = buildProperties([testKey: TEST_VALUE, anotherTestKey: ANOTHER_TEST_VALUE])
            def target = buildProperties([testKey: "replaceMe"])
            def result = buildProperties([testKey: TEST_VALUE, anotherTestKey: ANOTHER_TEST_VALUE])
        when:
            NetworkSetup.mergeProperties(source, target)
        then:
            result == target
    }

    def buildProperties(Map map) {
        Properties properties = new Properties()
        properties.putAll(map)

        return properties
    }
}
