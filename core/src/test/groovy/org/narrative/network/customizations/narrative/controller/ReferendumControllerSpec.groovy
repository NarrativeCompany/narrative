package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.common.datagen.ReferendumGen
import org.narrative.network.customizations.narrative.service.api.ReferendumService
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

class ReferendumControllerSpec extends WebMvcBaseSpec {

    @Shared ReferendumService referendumService = Mock(ReferendumService)

    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        referendumService = detachedMockFactory.Mock(ReferendumService)
        return new ReferendumController(referendumService)
    }

    @Override
    def getMockList() {
        return [referendumService]
    }

    @Unroll
    def "test findBallotBox"() {
        given:
            def uriString = bindParamsToUri('/referendums/ballot-box', ['page': 0, 'size': 1])
            def expected = ReferendumGen.buildReferendumPage(buildPageRequest(0, 1), 1)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<ReferendumDTO> dtoResult = convertResultToObjectPage(mvcResult, ReferendumDTO)
        then:
            1 * referendumService.findReferendums(_ as Pageable) >> expected
            mvcResult != null
            dtoResult != null
    }
}
