package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.NrveUsdValue
import org.narrative.network.customizations.narrative.NrveValue
import org.narrative.network.customizations.narrative.niches.nicheauction.BidStatus
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionBidDTO
import org.apache.commons.lang.math.RandomUtils

import java.time.Instant
import java.time.temporal.ChronoUnit

class NicheAuctionBidGen {

    static def buildNicheAuctionBid() {
        def oid = new OID(RandomUtils.nextLong())
        NicheAuctionBidDTO.builder()
                .oid(oid)
                .status(BidStatus.values()[RandomUtils.nextInt(BidStatus.values().size())])
                .bidDatetime(Instant.now().minus(RandomUtils.nextInt(100), ChronoUnit.MINUTES))
                .bidder(UserGen.buildUser())
                .bidAmount(new NrveUsdValue(new NrveValue(RandomUtils.nextInt(200000)), new BigDecimal("0.03")))
                .build()
    }

    static List<NicheAuctionBid> buildNicheAuctionBidList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildNicheAuctionBid())
        }
        res
    }
}
