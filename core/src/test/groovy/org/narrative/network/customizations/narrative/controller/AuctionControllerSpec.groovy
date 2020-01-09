package org.narrative.network.customizations.narrative.controller

import org.narrative.base.MockInvalidParamError
import org.narrative.base.WebMvcBaseSpec
import org.narrative.common.datagen.NicheAuctionBidGen
import org.narrative.common.datagen.NicheAuctionGen
import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.NrveValue
import org.narrative.network.customizations.narrative.controller.advice.ExceptionHandlingControllerAdvice
import org.narrative.network.customizations.narrative.controller.postbody.auction.NicheAuctionBidInputDTO
import org.narrative.network.customizations.narrative.controller.postbody.currency.NrveUsdPriceInputDTO
import org.narrative.network.customizations.narrative.service.api.AuctionService
import org.narrative.network.customizations.narrative.service.api.model.*
import org.apache.commons.lang.math.RandomUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Shared
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import java.time.Instant

class AuctionControllerSpec extends WebMvcBaseSpec {
    @Shared AuctionService auctionService

    /**
     * Defer controller creation to the implementing test spec
     */
    @Override
    def buildController(DetachedMockFactory detachedMockFactory) {
        auctionService = detachedMockFactory.Mock(AuctionService)

        return new AuctionController(auctionService)
    }

    /**
     * Get a set of all mocks used by the generated controller
     */
    @Override
    def getMockList() {
        return [auctionService]
    }

    @Unroll
    def "Test findActiveAuctions by page:#page and page size:#size pendingPayment:#pendingPayment"() {
        given:
            def uriString
            if (page != null) {
                uriString = bindParamsToUri('/auctions', ['page':page, 'size':size, 'pendingPayment':pendingPayment])
            } else {
                uriString = bindParamsToUri('/auctions', ['pendingPayment':pendingPayment])
                // Default to the pageable default specified on the controller method
                PageableDefault pageableDefault = extractPageableDefaultFromMethod(AuctionController, 'findActiveAuctions', Boolean.TYPE, Pageable)
                page = pageableDefault.page()
                size = pageableDefault.size()
            }
            PageDataDTO<NicheAuctionDTO> expected = NicheAuctionGen.buildNicheAuctionPage(size, buildPageRequest(page, size), pageResultCount)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            PageDataDTO<NicheAuctionDTO> resPage = convertResultToObjectPage(mvcResult, NicheAuctionDTO)
        then:
            1 * auctionService.findActiveAuctions(_ as Pageable, _) >> { args ->
                Pageable pageable = args[0]
                boolean  capturedPendingPayment = args[1]
                assert capturedPendingPayment == pendingPayment
                validatePageRequest(pageable, page, size)
                expected
            }
            mvcResult != null
            vaidatePageResult(resPage, expected, page, size, pageResultCount)
        where:
            page | size | pendingPayment
            0    | 1    | true
            1    | 5    | false
            2    | 3    | true
            null | null | false
    }

    def "Test findAuction"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def uriString = '/auctions/' + oid.toString()
            def expected = NicheAuctionGen.buildNicheAuctionDetail(oid)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            NicheAuctionDetailDTO res = convertResultToObject(mvcResult, NicheAuctionDetailDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * auctionService.findAuction(oid) >> expected
            mvcResult != null
            res == expected
    }

    def "Test findAuction not found"() {
        given:
            def oid = new OID(RandomUtils.nextLong());
            def uriString = '/auctions/' + oid.toString()
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            mockMvc.perform(buildGetRequest(uriString))
                    //.andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            1 * auctionService.findAuction(oid) >> {throw new MockInvalidParamError('')}
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
    }

    @Unroll
    def "Test findAuctionBids"() {
        given:
            def oid = OID.valueOf(RandomUtils.nextLong())
            def uriString = '/auctions/'+ oid.toString() + '/bids'

            def expected = NicheAuctionBidGen.buildNicheAuctionBidList(10)
        when:
            def mvcResult = mockMvc.perform(buildGetRequest(uriString))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            List<NicheAuctionBidDTO> results = convertResultToObjectList(mvcResult, NicheAuctionBidDTO)
        then:
            1 * auctionService.findAuctionBids(_) >> { args ->
                def capturedOid = args[0]
                capturedOid == oid
                expected
            }
            mvcResult != null
            assert results == expected
    }

    def "test findAuctionBids auction not found"() {
        given:
            def oid = new OID(RandomUtils.nextLong());
            def uriString = '/auctions/' + oid.toString() + '/bids'
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            mockMvc.perform(buildGetRequest(uriString))
                    //.andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            1 * auctionService.findAuctionBids(oid) >> {throw new MockInvalidParamError('')}
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
    }

    def "test bidOnActiveAuction auction not found"() {
        given:
            def oid = new OID(RandomUtils.nextLong());
            def uriString = '/auctions/' + oid.toString() + '/bids'
            def nrveUsdPrice = new BigDecimal("0.03")
            def now = Instant.now()
            def nrveUsdPriceInput = new NrveUsdPriceInputDTO(nrveUsdPrice, now, NrveUsdPriceFields.generateSecurityToken(nrveUsdPrice, now))
            def bidInput = NicheAuctionBidInputDTO.builder().maxNrveBid(BigDecimal.TEN).nrveUsdPrice(nrveUsdPriceInput).build()
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            mockMvc.perform(buildPostRequest(uriString, mappingJackson2JsonView.getObjectMapper().writeValueAsString(bidInput)))
                    //.andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
        then:
            //Stub out this static method call wrapper for testing
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            1 * auctionService.bidOnAuction(oid, _) >> {throw new MockInvalidParamError('')}
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
    }

    @Unroll
    def "Test bidOnActiveAuction invalid bind params oid:#oid, nrveBid:#nrveBid" () {
        given:
            def uriString = '/auctions/' + oid.toString() + '/bids'
            def bidInput
            def nrveUsdPrice = new BigDecimal("0.03")
            def now = Instant.now()
            def nrveUsdPriceInput = new NrveUsdPriceInputDTO(nrveUsdPrice, now, NrveUsdPriceFields.generateSecurityToken(nrveUsdPrice, now))
            if (nrveBid == null) {
                bidInput = NicheAuctionBidInputDTO.builder().maxNrveBid(null).nrveUsdPrice(nrveUsdPriceInput).build()
            } else {
                bidInput = NicheAuctionBidInputDTO.builder().maxNrveBid(new BigDecimal(nrveBid)).nrveUsdPrice(nrveUsdPriceInput).build()
            }
            logSuppressor.suppressLogs(ExceptionHandlingControllerAdvice)
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, mappingJackson2JsonView.getObjectMapper().writeValueAsString(bidInput)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            ValidationErrorDTO err = convertResultToObject(mvcResult, ValidationErrorDTO)
        then:
            1 * exceptionHandlingControllerAdvice.setCurrentPartitionGroupInError() >> {}
            mvcResult != null
            err.fieldErrors.size() == badfields.size
            extractKeyValues(err).containsAll(badfields)
        cleanup:
            logSuppressor.resumeLogs(ExceptionHandlingControllerAdvice)
        where:
            oid                             | nrveBid                       | badfields
            new OID(RandomUtils.nextLong()) | null                          | ['maxNrveBid']
            null                            | BigDecimal.valueOf(1000)  | ['auctionOid']
            null                            | null                          | ['auctionOid']
    }

    def "test bidOnActiveAuction"() {
        given:
            def oid = new OID(RandomUtils.nextLong())
            def bidNrve =  new NrveValue(1234.567)
            def uriString = '/auctions/' + oid.toString() + '/bids'
            def nrveUsdPrice = new BigDecimal("0.03")
            def now = Instant.now()
            def nrveUsdPriceInput = new NrveUsdPriceInputDTO(nrveUsdPrice, now, NrveUsdPriceFields.generateSecurityToken(nrveUsdPrice, now))
            def bidInput = NicheAuctionBidInputDTO.builder().maxNrveBid(new BigDecimal(bidNrve.getValue())).nrveUsdPrice(nrveUsdPriceInput).build()

            def expected = NicheAuctionGen.buildNicheAuctionDetail(oid)
        when:
            def mvcResult = mockMvc.perform(buildPostRequest(uriString, mappingJackson2JsonView.getObjectMapper().writeValueAsString(bidInput)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
            NicheAuctionDetailDTO res = convertResultToObject(mvcResult, NicheAuctionDetailDTO)
        then:
            //Stub out this static method call wrapper for testing
            1 * auctionService.bidOnAuction(oid, bidInput) >> expected
            mvcResult != null
            res == expected
    }
}
