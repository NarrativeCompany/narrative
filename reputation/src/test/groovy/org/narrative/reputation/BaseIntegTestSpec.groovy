package org.narrative.reputation

import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@Slf4j
@DirtiesContext
@SpringBootTest(classes = ReputationIntegTestApplication.class, webEnvironment=SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles ( profiles = ['default','local','integ'] )
abstract class BaseIntegTestSpec extends Specification {
}