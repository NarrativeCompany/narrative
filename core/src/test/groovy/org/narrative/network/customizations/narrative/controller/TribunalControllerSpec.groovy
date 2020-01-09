package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.common.datagen.TribunalMemberGen
import org.narrative.network.customizations.narrative.service.api.TribunalService
import org.narrative.network.customizations.narrative.service.api.model.UserDTO
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.mock.DetachedMockFactory

class TribunalControllerSpec extends WebMvcBaseSpec {
    @Shared TribunalService tribunalService

    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        tribunalService = detachedMockFactory.Mock(TribunalService)
        return new TribunalController(tribunalService)
    }

    @Override
    def getMockList() {
        return [tribunalService]
    }

    def "test getTribunalMembers"() {
        given:
            def uriString = '/tribunal/members'
            def expected = TribunalMemberGen.buildUserList(4)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            List<UserDTO> resUsers = convertResultToObjectList(mvcResult, UserDTO)
        then:
            1 * tribunalService.getTribunalMembers() >> expected
            mvcResult != null
            resUsers == expected
    }
}
