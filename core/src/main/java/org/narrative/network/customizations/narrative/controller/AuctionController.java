package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.auction.NicheAuctionBidInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.invoice.FiatPaymentInputDTO;
import org.narrative.network.customizations.narrative.service.api.AuctionService;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionBidDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

/**
 * Date: 8/24/18
 * Time: 9:55 AM
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/auctions")
@Validated
public class AuctionController {
    public static final String AUCTION_OID_PARAM = "auctionOid";
    public static final String AUCTION_OID_PARAMSPEC = "{" + AUCTION_OID_PARAM + "}";

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    /**
     * Find all auctions by page
     *
     * @param pageRequest Page information for the request
     * @return {@link PageDataDTO} of {@link NicheAuctionDTO}
     */
    @GetMapping
    public PageDataDTO<NicheAuctionDTO> findActiveAuctions(@RequestParam(defaultValue = "false") boolean pendingPayment, @PageableDefault(size = 50) Pageable pageRequest) {
        return auctionService.findActiveAuctions(pageRequest, pendingPayment);
    }

    /**
     * Find an auction by OID
     *
     * @param auctionOid OID for the auction of interest
     * @return {@link NicheAuctionDetailDTO}
     */
    @GetMapping("/" + AUCTION_OID_PARAMSPEC)
    public NicheAuctionDetailDTO findAuction(@PathVariable(AUCTION_OID_PARAM) OID auctionOid) {
        return auctionService.findAuction(auctionOid);
    }

    /**
     * Find the bids for an auction by OID
     *
     * @param auctionOid OID for the auction of interest
     * @return {@link NicheAuctionDTO}
     */
    @GetMapping("/" + AUCTION_OID_PARAMSPEC + "/bids")
    public List<NicheAuctionBidDTO> findAuctionBids(@PathVariable(AUCTION_OID_PARAM) OID auctionOid) {
        return auctionService.findAuctionBids(auctionOid);
    }

    /**
     * Place a bid on an auction
     *
     * @param auctionOid The {@link OID} corresponding to the Auction to bid on.
     * @param bidInput   The maximum amount of NRVE being bid.
     * @return The {@link NicheAuctionDetailDTO} after the bid was made.
     */
    @PostMapping(path = "/" + AUCTION_OID_PARAMSPEC + "/bids")
    public NicheAuctionDetailDTO bidOnActiveAuction(@PathVariable(AUCTION_OID_PARAM) OID auctionOid,
                                                    @Valid @RequestBody NicheAuctionBidInputDTO bidInput) {
        return auctionService.bidOnAuction(auctionOid, bidInput);
    }

    /**
     * Place a security deposit on an auction
     *
     * @param auctionOid The {@link OID} corresponding to the Auction to place the deposit onto
     * @param payment The {@link FiatPaymentInputDTO} details for this deposit
     * @return The {@link NicheAuctionDetailDTO} after the deposit was made.
     */
    @PostMapping(path = "/" + AUCTION_OID_PARAMSPEC + "/security-deposit")
    public NicheAuctionDetailDTO placeSecurityDepositOnActiveAuction(
            @PathVariable(AUCTION_OID_PARAM) OID auctionOid,
            @Valid @RequestBody FiatPaymentInputDTO payment
    ) {
        return auctionService.placeSecurityDepositOnAuction(auctionOid, payment);
    }

    /**
     * End an auction.
     * NOTE: Not supported on production environments.
     *
     * @param auctionOid The {@link OID} of the auction to end.
     * @return {@link NicheAuctionDetailDTO} the update Niche Auction details
     */
    @PutMapping(path = "/" + AUCTION_OID_PARAMSPEC + "/end")
    public NicheAuctionDetailDTO endAuction(@PathVariable(AUCTION_OID_PARAM) OID auctionOid) {
        return auctionService.endAuction(auctionOid);
    }
}
