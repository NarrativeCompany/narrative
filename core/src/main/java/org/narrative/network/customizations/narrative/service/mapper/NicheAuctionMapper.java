package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionBidDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDetailDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class, NicheMapper.class, NicheDerivativeMapper.class})
public interface NicheAuctionMapper {
    /**
     * Map from {@link NicheAuction} entity to {@link NicheAuctionDTO}.
     *
     * @param nicheAuctionEntity The incoming niche auction entry entity to map
     * @return The mapped niche auction
     */
    @Mapping(source = "nrveUsdPriceFields", target = NicheAuctionDTO.Fields.nrveUsdPrice)
    NicheAuctionDTO mapNicheAuctionEntityToNicheAuction(NicheAuction nicheAuctionEntity);

    /**
     * Map from {@link NicheAuction} entity to {@link NicheAuctionDetailDTO}.
     *
     * @param auction The incoming niche auction entry entity to map
     * @return The mapped niche auction detail object
     */
    @Mapping(source = "auction", target = NicheAuctionDetailDTO.Fields.auction)
    @Mapping(source = "latestBidForCurrentUser.status", target = NicheAuctionDetailDTO.Fields.currentUserLatestBidStatus)
    @Mapping(source = "latestBidForCurrentUser.maxBidAmount", target = NicheAuctionDetailDTO.Fields.currentUserLatestMaxNrveBid)
    NicheAuctionDetailDTO mapNicheAuctionEntityToNicheAuctionDetail(NicheAuction auction);

    /**
     * Map a {@link List} of {@link NicheAuction} entities to a {@link List} of {@link NicheAuctionDTO}.
     *
     * @param auctionEntityList The incoming list of Bid entities
     * @return The resulting list of bids
     */
    default List<NicheAuctionDTO> mapAuctionEntityListToAuctionDtoList(Collection<NicheAuction> auctionEntityList, Map<NicheAuction, Long> bidCountsByAuction) {
        // jw: note: this is basically a implementation that mirrors what mapstruct would have done, I just added the code
        //     below to cache the bid counts on each NicheAuction prior to DTO mapping.
        if (auctionEntityList == null) {
            return null;
        }

        // jw: let's fetch all of the bidding counts once up front, and then set those onto the auctions directly so that
        //     we do not have to hit the DB separately for each one!
        List<NicheAuctionDTO> list = new ArrayList<>(auctionEntityList.size());
        for (NicheAuction auction : auctionEntityList) {
            Long count = bidCountsByAuction == null ? null : bidCountsByAuction.get(auction);
            auction.cacheTotalBidCount(count == null ? 0 : count);

            list.add(mapNicheAuctionEntityToNicheAuction(auction));
        }

        return list;
    }

    /**
     * Map from {@link NicheAuctionBid} entity to {@link NicheAuctionBidDTO}.
     *
     * @param nicheAuctionBidEntity The incoming niche auction bid entry entity to map
     * @return The mapped niche auction bid
     */
    @Mapping(source = "bidder.areaUser.user", target = "bidder")
    NicheAuctionBidDTO mapNicheAuctionBidEntityToNicheAuctionBid(NicheAuctionBid nicheAuctionBidEntity);

    /**
     * Map a {@link List} of {@link NicheAuctionBid} to a {@link List} of {@link NicheAuctionBidDTO}.
     *
     * @param nicheAuctionBidList The incoming list of niche auction bid entities
     * @return The resulting mapped list of niche auction bid DTOs
     */
    List<NicheAuctionBidDTO> mapNicheAuctionBidEntityListToNicheAuctionBidList(List<NicheAuctionBid> nicheAuctionBidList);
}
