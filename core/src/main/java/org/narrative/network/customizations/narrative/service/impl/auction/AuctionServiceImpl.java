package org.narrative.network.customizations.narrative.service.impl.auction;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.AuctionController;
import org.narrative.network.customizations.narrative.controller.postbody.auction.NicheAuctionBidInputDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheList;
import org.narrative.network.customizations.narrative.niches.niche.services.forcetasks.EndAuctionTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.BidOnAuctionTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessNicheAuctionSecurityDepositFiatPaymentTask;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.AuctionService;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionBidDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.FiatPaymentInput;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.mapper.NicheAuctionMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 8/24/18
 * Time: 1:06 PM
 *
 * @author jonmark
 */
@Service
@Transactional
public class AuctionServiceImpl implements AuctionService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final NicheAuctionMapper nicheAuctionMapper;

    public AuctionServiceImpl(AreaTaskExecutor areaTaskExecutor, NicheAuctionMapper nicheAuctionMapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.nicheAuctionMapper = nicheAuctionMapper;
    }

    /**
     * Find active auctions, page size and page.
     *
     * @param pageRequest Paging information for this request
     * @return {@link Slice} of {@link NicheAuctionDTO} found
     */
    @Override
    public PageDataDTO<NicheAuctionDTO> findActiveAuctions(Pageable pageRequest, boolean pendingPayment) {
        NicheList criteria = new NicheList();

        // jw: we need to apply the pageRequest to the criteria
        PageUtil.mutateCriteriaListWithPagingCriteria(criteria, pageRequest);

        // jw: setup the criteria for active auctions.
        if (pendingPayment) {
            criteria.setPendingPurchase(true);

        } else {
            criteria.setForSale(true);
        }
        criteria.doSortByField(NicheList.SortField.ACTIVE_AUCTION_EXPIRATION_DATE);
        criteria.setSortAsc(true);

        // Execute the task to obtain niche information
        List<NicheAuctionDTO> res = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<NicheAuctionDTO>>(false) {
            @Override
            protected List<NicheAuctionDTO> doMonitoredTask() {
                // jw: Execute the task to obtain niche information
                List<Niche> niches = areaTaskExecutor.executeAreaTask(criteria);

                if (niches.isEmpty()) {
                    return Collections.emptyList();
                }

                // jw: if the requester is a logged in user, let's populate the followedByCurrentUser flag on all niches.
                if (getNetworkContext().isLoggedInUser()) {
                    FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(getNetworkContext().getUser(), niches);
                }

                List<NicheAuction> auctions = new ArrayList<>(niches.size());
                for (Niche niche : niches) {
                    auctions.add(niche.getActiveAuction());
                }

                //Map the entity results into niche DTOs
                return nicheAuctionMapper.mapAuctionEntityListToAuctionDtoList(auctions, NicheAuction.dao().getBidCountsForAuctions(auctions));
            }
        });

        // jw: Build a page from the results
        return PageUtil.buildPage(res, pageRequest, criteria.getCount());
    }

    /**
     * Find an auction by OID.
     *
     * @param auctionOid OID for the auction of interest
     * @return NicheAuctionDTO found
     */
    @Override
    public NicheAuctionDetailDTO findAuction(OID auctionOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<NicheAuctionDetailDTO>(false) {
            @Override
            protected NicheAuctionDetailDTO doMonitoredTask() {
                NicheAuction auction = NicheAuction.dao().getForApiParam(auctionOid, AuctionController.AUCTION_OID_PARAM);

                return nicheAuctionMapper.mapNicheAuctionEntityToNicheAuctionDetail(auction);
            }
        });
    }

    @Override
    public List<NicheAuctionBidDTO> findAuctionBids(OID auctionOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<NicheAuctionBidDTO>>(false) {
            @Override
            protected List<NicheAuctionBidDTO> doMonitoredTask() {
                NicheAuction auction = NicheAuction.dao().getForApiParam(auctionOid, AuctionController.AUCTION_OID_PARAM);

                // jw: get all auction bids from this auction ordered by bid datetime desc.
                return nicheAuctionMapper.mapNicheAuctionBidEntityListToNicheAuctionBidList(auction.getAuctionBids());
            }
        });
    }

    public NicheAuctionDetailDTO bidOnAuction(OID auctionOid, NicheAuctionBidInputDTO bidInput) {
        NicheAuction auction = NicheAuction.dao().getForApiParam(auctionOid, AuctionController.AUCTION_OID_PARAM);

        BidOnAuctionTask bidOnAuctionTask = new BidOnAuctionTask(auction.getNiche(), areaContext().getAreaUserRlm(), bidInput.getMaxNrveBid(), bidInput.getNrveUsdPrice());

        areaTaskExecutor.executeAreaTask(bidOnAuctionTask);

        // jw: Due to apollo caching, we need to return the NicheAuctionDetail so that the UI will update properly
        return nicheAuctionMapper.mapNicheAuctionEntityToNicheAuctionDetail(auction);
    }

    @Override
    public NicheAuctionDetailDTO placeSecurityDepositOnAuction(OID auctionOid, FiatPaymentInput payment) {
        NicheAuction auction = NicheAuction.dao().getForApiParam(auctionOid, AuctionController.AUCTION_OID_PARAM);

        areaTaskExecutor.executeAreaTask(new ProcessNicheAuctionSecurityDepositFiatPaymentTask(
                payment.getProcessorType()
                , payment.getPaymentToken()
                , auction
        ));

        // jw: Due to apollo caching, we need to return the NicheAuctionDetail so that the UI will update properly
        return nicheAuctionMapper.mapNicheAuctionEntityToNicheAuctionDetail(auction);
    }

    @Override
    public NicheAuctionDetailDTO endAuction(OID auctionOid) {
        NicheAuction auction = NicheAuction.dao().getForApiParam(auctionOid, AuctionController.AUCTION_OID_PARAM);
        areaTaskExecutor.executeAreaTask(new EndAuctionTask(auction));
        return nicheAuctionMapper.mapNicheAuctionEntityToNicheAuctionDetail(auction);
    }
}
