package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.common.datagen.ElectionGen
import org.narrative.common.persistence.OID
import org.narrative.config.properties.NarrativeProperties
import org.narrative.network.customizations.narrative.controller.postbody.election.ElectionNominationInputDTO
import org.narrative.network.customizations.narrative.elections.ElectionStatus
import org.narrative.network.customizations.narrative.elections.ElectionType
import org.narrative.network.customizations.narrative.elections.NomineeStatus
import org.narrative.network.customizations.narrative.service.api.ElectionService
import org.narrative.network.customizations.narrative.service.api.model.ElectionDetailDTO
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineesDTO
import org.apache.commons.lang.math.RandomUtils
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.mock.DetachedMockFactory

/**
 * Date: 11/14/18
 * Time: 9:47 AM
 *
 * @author jonmark
 */
class ElectionControllerSpec extends WebMvcBaseSpec {

    @Shared
    ElectionService electionService = Mock(ElectionService)
    @Shared
    NarrativeProperties narrativeProperties = new NarrativeProperties()

    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        // jw: let's set this up with an expected default.
        narrativeProperties.getSpring().getMvc().maxPageSize = 200
        electionService = detachedMockFactory.Mock(ElectionService)

        return new ElectionController(electionService, narrativeProperties)
    }

    @Override
    def getMockList() {
        return [electionService]
    }

    def "test findElectionNominees"() {
        given:
            def count = 3
            def oid = new OID(RandomUtils.nextLong())
            def uriString = bindParamsToUri('/elections/' + oid + '/nominees', ['count': count])
            def expected = ElectionGen.buildElectionNominees(count)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ElectionNomineesDTO res = convertResultToObject(mvcResult, ElectionNomineesDTO)
        then:
            1 * electionService.findElectionNominees(oid, null, count) >> expected
            mvcResult != null
            res == expected
    }

    def "test nominateCurrentUser"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = bindParamsToUri('/elections/' + oid + '/nominees/current-user')
            def personalStatement = 'test statement'
            def nomineeInput = ElectionNominationInputDTO.builder().personalStatement(personalStatement).build()
            def expected = ElectionGen.buildElectionDetailWithNominee(ElectionType.NICHE_MODERATOR, ElectionStatus.NOMINATING, NomineeStatus.CONFIRMED)
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, objectMapper.writeValueAsString(nomineeInput)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ElectionDetailDTO res = convertResultToObject(mvcResult, ElectionDetailDTO)
        then:
            1 * electionService.nominateCurrentUser(oid, personalStatement) >> expected
            mvcResult != null
            res == expected
    }

    def "test withdrawNominationForCurrentUser"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = bindParamsToUri('/elections/' + oid + '/nominees/current-user')
            def expected = ElectionGen.buildElectionDetail(ElectionType.NICHE_MODERATOR, ElectionStatus.NOMINATING)
        when:
            def mvcResult = mockMvc.perform(buildDeleteRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ElectionDetailDTO res = convertResultToObject(mvcResult, ElectionDetailDTO)
        then:
            1 * electionService.withdrawNominationForCurrentUser(oid) >> expected
            mvcResult != null
            res == expected
    }
}
