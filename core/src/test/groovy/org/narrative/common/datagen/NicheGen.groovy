package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO
import org.narrative.network.customizations.narrative.service.api.model.NicheDetailDTO
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang3.RandomStringUtils

/**
 * Test data generator for {@link NicheDTO}
 */
class NicheGen {
    static NicheDTO buildNiche(OID oid = null, String prettyUrlString = null) {
        oid = oid == null ? new OID(RandomUtils.nextLong()) : oid
        NicheDTO.builder()
                .name(RandomStringUtils.random(20, true, true))
                .oid(oid)
                .prettyUrlString(prettyUrlString)
                .build()
    }

    static NicheDetailDTO buildNicheDetail(OID oid = null, String prettyUrlString = null) {
        NicheDetailDTO.builder()
                .niche(buildNiche(oid, prettyUrlString))
                .build()
    }

    static List<NicheDTO> buildNicheList(count) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildNiche())
        }
        res
    }

    static PageDataDTO<NicheDTO> buildNichePage(count, pageRequest, totalSize) {
        PageUtil.buildPage(buildNicheList(count), pageRequest, totalSize)
    }
}
