package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.auction.NicheAuctionBidInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionBidDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.FiatPaymentInput;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Date: 8/24/18
 * Time: 12:57 PM
 *
 * @author jonmark
 */
public interface AuctionService {
    /**
     * Find active auctions, page size and page.
     *
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link NicheAuctionDTO} found
     */
    PageDataDTO<NicheAuctionDTO> findActiveAuctions(Pageable pageRequest, boolean pendingPayment);

    /**
     * Find an auction by OID.
     *
     * @param auctionOid OID for the auction of interest
     * @return NicheAuctionDTO found
     */
    NicheAuctionDetailDTO findAuction(OID auctionOid);

    /**
     * Find bids for an auction by auction OID.
     *
     * @param auctionOid OID for the auction of interest
     * @return {@link List} of {@link NicheAuctionBidDTO} found
     */
    List<NicheAuctionBidDTO> findAuctionBids(OID auctionOid);

    /**
     * Place a bid on a active auction as the user who is in scope.
     *
     * @param auctionOid the {@link OID} corresponding to the auction being bidded on.
     * @param bidInput    The maximum amount of NRVE being bid
     * @return The leading {@link NicheAuctionBidDTO} after the bid was placed.
     */
    NicheAuctionDetailDTO bidOnAuction(OID auctionOid, NicheAuctionBidInputDTO bidInput);

    /**
     * Place a security deposit on a active auction as the user who is in scope.
     *
     * @param auctionOid the {@link OID} corresponding to the auction being bidded on.
     * @param payment    The details for the payment that was made
     * @return The leading {@link NicheAuctionBidDTO} after the security deposit has been made
     */
    NicheAuctionDetailDTO placeSecurityDepositOnAuction(OID auctionOid, FiatPaymentInput payment);

    /**
     * End an auction.
     * @param auctionOid the {@link OID} of the auction to end
     * @return the updated {@link NicheAuctionDetailDTO}
     */
    NicheAuctionDetailDTO endAuction(OID auctionOid);
}
