package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil

class ReferendumGen {

    static ReferendumDTO buildReferendum() {
        ReferendumDTO.builder()
            .oid(new OID())
            .votePointsAgainst("1.00")
            .votePointsFor("2.00")
            .commentCount(3)
            .niche(null)
            .build()
    }

    static List<ReferendumDTO> buildReferendumList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildReferendum())
        }
        res
    }

    static PageDataDTO<ReferendumDTO> buildReferendumPage(pageRequest, totalSize) {
        PageUtil.buildPage(buildReferendumList(), pageRequest, totalSize)
    }

}
