package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.config.properties.NarrativeProperties
import org.narrative.network.customizations.narrative.service.api.SearchService
import spock.lang.Shared
import spock.mock.DetachedMockFactory

class SearchGenControllerSpec extends WebMvcBaseSpec {

    @Shared SearchService searchService = Mock(SearchService)
    @Shared NarrativeProperties narrativeProperties = new NarrativeProperties()

    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        // jw: let's set this up with an expected default.
        narrativeProperties.getSpring().getMvc().maxPageSize = 200
        searchService = detachedMockFactory.Mock(SearchService)

        return new SearchController(searchService, narrativeProperties)
    }

    @Override
    def getMockList() {
        return [searchService]
    }

    // TODO mremi Resolve jackson.databind.exc.MismatchedInputException.
    /*@Unroll
    def "Test search"() {
        given:
            def uriString
            if (page != null) {
                uriString = bindParamsToUri('/search', ['keyword': keyword, 'filter': searchType, 'page': page, 'size': size])
            } else {
                // Default to the pageable default specified on the controller method
                PageableDefault pageableDefault = extractPageableDefaultFromMethod(SearchController, 'find', Pageable)
                page = pageableDefault.page()
                size = pageableDefault.size()
            }
            PageDataDTO<UserDTO> expected = SearchGen.buildUserPage(size, buildPageRequest(page, size), pageResultCount)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
//                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<UserDTO> resPage = convertResultToObjectPage(mvcResult, UserDTO)
        then:
            1 * searchService.find(_ as Pageable, _) >> { args ->
                Pageable pageable = args[0]
                String searchKeyword = args[1]
                assert searchKeyword == keyword
                SearchType searchSearchType = args[2]
                assert searchSearchType == searchType
                validatePageRequest(pageable, page, size)
                expected
            }
            mvcResult != null
            vaidatePageResult(resPage, expected, page, size, pageResultCount)
        where:
            page | size | keyword | searchType
            1    | 1    | "mark"  | SearchType.MEMBERS
    }*/
}
