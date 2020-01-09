package org.narrative.network.customizations.narrative.controller

import org.narrative.base.WebMvcBaseSpec
import org.narrative.common.datagen.ElectionGen
import org.narrative.common.datagen.NicheGen
import org.narrative.common.datagen.ReferendumGen
import org.narrative.common.datagen.UserGen
import org.narrative.common.persistence.OID
import org.narrative.config.properties.NarrativeProperties
import org.narrative.network.core.content.base.SEOObject
import org.narrative.network.customizations.narrative.controller.advice.ExceptionHandlingControllerAdvice
import org.narrative.network.customizations.narrative.controller.postbody.niche.CreateNicheInputDTO
import org.narrative.network.customizations.narrative.controller.postbody.niche.SimilarNicheSearchInputDTO
import org.narrative.network.customizations.narrative.controller.postbody.niche.UpdateNicheInputDTO
import org.narrative.network.customizations.narrative.elections.ElectionStatus
import org.narrative.network.customizations.narrative.niches.niche.Niche
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheDAO
import org.narrative.network.customizations.narrative.niches.niche.services.NicheList
import org.narrative.network.customizations.narrative.service.api.NicheService
import org.narrative.network.customizations.narrative.service.api.model.*
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

class NicheControllerSpec extends WebMvcBaseSpec {
    @Shared NicheService nicheService
    @Shared NarrativeProperties narrativeProperties = new NarrativeProperties()

    /**
     * Defer controller creation to the implementing test spec
     */
    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        nicheService = detachedMockFactory.Mock(NicheService)

        return new NicheController(nicheService, narrativeProperties, staticMethodWrapper)
    }

    /**
     * Get a set of all mocks used by the generated controller
     */
    @Override
    def getMockList() {
        return [nicheService]
    }

    @Unroll
    def "Test findNiches by page:#page and page size:#size"() {
        given:
            def uriString
            if (page != null) {
                uriString = bindParamsToUri('/niches', ['page':page, 'size':size])
            } else {
                uriString = '/niches'
                // Default to the pageable default specified on the controller method
                PageableDefault pageableDefault = extractPageableDefaultFromMethod(NicheController, 'findNiches', Pageable)
                page = pageableDefault.page()
                size = pageableDefault.size()
            }
            def expected = NicheGen.buildNichePage(size, buildPageRequest(page, size), 10)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<NicheDTO> resPage = convertResultToObjectPage(mvcResult, NicheDTO)
        then:
            1 * nicheService.findNiches(_ as NicheList, _ as Pageable) >> {args ->
                NicheList nicheList = args[0]
                Pageable pageable = args[1]
                validatePageRequest(pageable, page, size)
                nicheList.notStatus == NicheStatus.REJECTED
                nicheList.sort == NicheList.SortField.LAST_STATUS_UPDATE_DATETIME
                expected
            }
            mvcResult != null
            vaidatePageResult(resPage, expected, page, size, pageResultCount)
            resPage != null
            resPage.items == expected.items
            resPage.info == expected.info
            resPage.info.totalElements == expected.info.totalElements
            resPage.info.number == page
            resPage.info.size == size

        where:
            page | size
            0    | 1
            1    | 5
            2    | 3
            null | null
    }

    def "Test findNiche"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def prettyUrlString = 'test-url-id'
            def uriString = '/niches/' + SEOObject.SEO_ID_PREFIX + prettyUrlString
            def expected = NicheGen.buildNicheDetail(oid, prettyUrlString)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            NicheDetailDTO res = convertResultToObject(mvcResult, NicheDetailDTO)
        then:
            1 * nicheService.findNicheByUnknownId(SEOObject.SEO_ID_PREFIX + prettyUrlString) >> expected
            mvcResult != null
            res == expected
    }

    def "Test findNiche not found"() {
        given:
            def prettyUrlString = 'test-url-id';
            def uriString = '/niches/' + SEOObject.SEO_ID_PREFIX + prettyUrlString
        when:
            mockMvc.perform(buildGetRequest(uriString))
                    //.andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
        then:
            1 * nicheService.findNicheByUnknownId(SEOObject.SEO_ID_PREFIX + prettyUrlString) >> null
    }

    @Unroll
    def "Test find similar POST by name:#name and description:#description"() {
        given:
            def uriString = "/niches/similar"
            def expected = NicheGen.buildNicheList(5)
            def nicheInput = SimilarNicheSearchInputDTO.builder().name(name).description(description).build()
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, objectMapper.writeValueAsString(nicheInput)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            List<NicheDTO> res = convertResultToObjectList(mvcResult, NicheDTO)
        then:
            1 * nicheService.findSimilarNiches(nicheInput) >> expected
            mvcResult != null
            res  == expected
        where:
            name        | description
            'someName'  | 'someDescription'
    }

    @Unroll
    def "Test find similar POST by name:#name and description:#description and badfields:#badfields - validation failure"() {
        given:
            def uriString = "/niches/similar"
            def postObj = ['name': name, 'description': description]
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, objectMapper.writeValueAsString(postObj)))
                    //.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == badfields.size
            extractKeyValues(err).containsAll(badfields)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            [name, description, badfields] << buildBadRequestData()
    }

    def "Test find similar by OID"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = '/niches/similar/' + oid.toString()
            def expected = NicheGen.buildNicheList(5)
            def nicheOids = []
            for (NicheDTO nicheDTO : expected) {
                nicheOids.push(nicheDTO.getOid())
            }
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
            List<NicheDTO> res = convertResultToObjectList(mvcResult, NicheDTO)
        then:
            1 * nicheService.findSimilarNicheOids(oid) >> nicheOids
            1 * nicheService.getNicheDTOsForNicheOids(nicheOids) >> expected
            mvcResult != null
            res == expected
    }

    def "Test find similar by OID not found"() {
        given:
            def oid = new OID(RandomUtils.nextLong());
            def uriString = '/niches/similar/' + oid.toString()
            def nicheOids = Collections.emptyList()
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
        then:
            1 * nicheService.findSimilarNicheOids(oid) >> nicheOids
            1 * nicheService.getNicheDTOsForNicheOids(nicheOids) >> Collections.emptyList()
            mvcResult != null
            mvcResult.response.contentLength == 0
    }

    def "Test create niche"() {
        given:
            def uriString = "/niches"
            def expected = ReferendumGen.buildReferendum()
            def nicheInput = CreateNicheInputDTO.builder().name('someName').description('someDescription').assertChecked(true).agreeChecked(true).build()
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, objectMapper.writeValueAsString(nicheInput)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ReferendumDTO res = convertResultToObject(mvcResult, ReferendumDTO)
        then:
            1 * nicheService.createNiche(nicheInput) >> expected
            mvcResult != null
            res  == expected
    }

    @Unroll
    def "Test create niche name:#name, description:#description, badfields:#badfields and assertChecked:#assertChecked and agreeChecked:#agreeChecked - validation failure"() {
        given:
            def uriString = "/niches"
            def postObj = ['name': name, 'description': description, 'assertChecked': assertChecked, 'agreeChecked': agreeChecked]
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, objectMapper.writeValueAsString(postObj)))
                    //.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == badfields.size
            extractKeyValues(err).containsAll(badfields)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            [name, description, badfields, assertChecked, agreeChecked] << buildBadRequestDataVerify()
    }

    def "Test update niche"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = "/niches/" + oid.toString()
            def expected = TribunalIssueDetailDTO.builder().tribunalIssue(TribunalIssueDTO.builder().oid(oid).build()).build()
            def putObj = UpdateNicheInputDTO.builder().name('someName').description('someDescription').build()
            def putObjJson = objectMapper.writeValueAsString(putObj)
            def niche = new Niche()
            NicheDAO nicheDAO = Mock(NicheDAO)
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, putObjJson))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            TribunalIssueDetailDTO res = convertResultToObject(mvcResult, TribunalIssueDetailDTO)
        then:
            1 * staticMethodWrapper.getNicheDAO() >> nicheDAO
            1 * nicheDAO.getForApiParam(oid, "nicheOid") >> niche
            1 * nicheService.submitNicheUpdateRequest(niche, putObj) >> expected
            mvcResult != null
            res  == expected
    }

    @Unroll
    def "Test update niche name:#name, description:#description, badfields:#badfields and oid:#oid - validation failure"() {
        given:
            def uriString = "/niches/" + oid.toString()
            def postObj = ['name': name, 'description': description]
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            def mvcResult = mockMvc.perform(buildPutRequest(uriString, objectMapper.writeValueAsString(postObj)))
                    //.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == badfields.size
            extractKeyValues(err).containsAll(badfields)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            [name, description, badfields, oid] << buildBadRequestDataUpdate()
    }

    def "Test getRandomNicheFollowers"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = '/niches/' + oid.toString() + '/followers'
            def numItems = 5
            def expected = UserGen.buildUserPage(numItems, buildPageRequest(0, numItems), numItems)
            narrativeProperties.getSpring().getMvc().maxPageSize = 20
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString, ['limit':numItems]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<UserDTO> resPage = convertResultToObjectPage(mvcResult, UserDTO)
        then:
            1 * nicheService.getRandomNicheFollowers(oid, numItems) >> expected
            mvcResult != null
            resPage != null
            resPage.items == expected.items
            resPage.info.number == 0
            resPage.info.size == 5
    }

    @Unroll
    def "test findModeratorElections"() {
        given:
            def uriString = bindParamsToUri('/niches/moderator-elections', ['page': 0, 'size': 1])
            def expected = ElectionGen.buildNicheModeratorElectionPage(buildPageRequest(0, 1), 1, ElectionStatus.NOMINATING)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<NicheModeratorElectionDTO> dtoResult = convertResultToObjectPage(mvcResult, NicheModeratorElectionDTO)
        then:
            1 * nicheService.findNicheModeratorElections(_ as Pageable) >> expected
            mvcResult != null
            dtoResult != null
    }

    @Unroll
    def "test findModeratorElection"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = '/niches/moderator-elections/' + oid
            def expected = ElectionGen.buildNicheModeratorElectionDetail(ElectionStatus.NOMINATING, oid)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            NicheModeratorElectionDetailDTO res = convertResultToObject(mvcResult, NicheModeratorElectionDetailDTO)
        then:
            1 * nicheService.findNicheModeratorElectionByOid(oid) >> expected
            mvcResult != null
            res == expected
    }

    def buildBadRequestData() {
        def res =
        [
                ['some !Name', 'someGoodDescription', ['name']],
                ['some ^Name', 'someGoodDescription', ['name']],
                ['some *Name', 'someGoodDescription', ['name']],
                ['some #Name', 'someGoodDescription', ['name']],
                ['some@Name', 'tooSmall', ['name', 'description']],
                [RandomStringUtils.randomAlphanumeric(Niche.MAX_NAME_LENGTH + 1), 'someGoodDescription',['name']],
                ['someName', RandomStringUtils.randomAlphanumeric(Niche.MAX_DESCRIPTION_LENGTH + 1), ['description']]
        ]
        res.add([null, null, ['name', 'description']])
        res.add(['so', null, ['name', 'description']],)
        res
    }

    def buildBadRequestDataVerify() {
        def badData = buildBadRequestData()
        badData.each{
            // bl: these are for assertChecked and agreeChecked
            it.add('true')
            it.add('true')
        }
        badData.add(['someGoodName', 'someGoodDescription', ['assertChecked','agreeChecked'], 'false', 'false'])
        badData
    }

    def buildBadRequestDataUpdate() {
        def badData = buildBadRequestData()
        badData.each{
            it.add(new OID(RandomUtils.nextLong()).value)
        }
    }
}
