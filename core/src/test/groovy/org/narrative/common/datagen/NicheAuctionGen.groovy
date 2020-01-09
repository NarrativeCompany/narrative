package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDTO
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionDetailDTO
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil
import org.apache.commons.lang.math.RandomUtils

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

class NicheAuctionGen {

    static def buildNicheAuction(OID oid = null) {
        oid = oid == null ? new OID(RandomUtils.nextLong()) : oid
        NicheAuctionDTO.builder()
                .niche(NicheGen.buildNiche())
                .startDatetime(new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(RandomUtils.nextInt(5))))
                .endDatetime(new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(RandomUtils.nextInt(5))))
                .totalBidCount(RandomUtils.nextInt(100))
                .leadingBid(NicheAuctionBidGen.buildNicheAuctionBid())
                .oid(oid)
                .build()
    }

    static def buildNicheAuctionDetail(OID oid = null) {
        NicheAuctionDetailDTO.builder()
                .auction(buildNicheAuction(oid))
                .build()
    }

    static List<NicheAuctionDTO> buildNicheAuctionList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildNicheAuction())
        }
        res
    }

    static PageDataDTO<NicheAuctionDTO> buildNicheAuctionPage(count, pageRequest, totalSize) {
        PageUtil.buildPage(buildNicheAuctionList(count), pageRequest, totalSize)
    }
}
