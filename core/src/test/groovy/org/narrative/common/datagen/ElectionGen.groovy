package org.narrative.common.datagen

import org.narrative.common.persistence.OID
import org.narrative.network.customizations.narrative.elections.ElectionStatus
import org.narrative.network.customizations.narrative.elections.ElectionType
import org.narrative.network.customizations.narrative.elections.NomineeStatus
import org.narrative.network.customizations.narrative.service.api.model.*
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil
import org.apache.commons.lang.math.RandomUtils
import org.springframework.data.domain.Pageable

import java.time.Instant

/**
 * Date: 11/14/18
 * Time: 9:50 AM
 *
 * @author jonmark
 */
class ElectionGen {

    static ElectionDTO buildElection(ElectionType type, ElectionStatus status, OID oid = null) {
        ElectionDTO.builder()
                .oid(oid != null ? oid : new OID(RandomUtils.nextLong()))
                .type(type)
                .status(status)
        // jw: for now, let's just default this to 2, if a test needs an override we can address that through the
        //     the method signature.
                .availableSlots(2)
                .nomineeCount(0)
                .nominationStartDatetime(Instant.now())
                .build()
    }

    static ElectionDetailDTO buildElectionDetail(ElectionType type, ElectionStatus status, OID oid = null, ElectionNomineeDTO currentUserNominee = null) {
        oid = oid != null ? oid : new OID(RandomUtils.nextLong())
        ElectionDetailDTO.builder()
                .oid(oid)
                .election(buildElection(type, status, oid))
                .currentUserNominee(currentUserNominee)
                .build()
    }

    static ElectionDetailDTO buildElectionDetailWithNominee(ElectionType type, ElectionStatus status, NomineeStatus nomineeStatus) {
        buildElectionDetail(type, status, null, buildElectionNominee(nomineeStatus))
    }

    static NicheModeratorElectionDTO buildNicheModeratorElection(ElectionStatus status) {
        def oid = new OID(RandomUtils.nextLong())
        NicheModeratorElectionDTO.builder()
                .oid(oid)
                .niche(NicheGen.buildNiche(null, "test-niche"))
                .election(buildElection(ElectionType.NICHE_MODERATOR, status, oid))
                .build()
    }

    static NicheModeratorElectionDetailDTO buildNicheModeratorElectionDetail(ElectionStatus status, OID oid) {
        NicheModeratorElectionDetailDTO.builder()
                .oid(oid)
                .niche(NicheGen.buildNiche(null, "test-niche"))
                .election(buildElectionDetail(ElectionType.NICHE_MODERATOR, status, oid))
                .build()
    }

    static ElectionNomineeDTO buildElectionNominee(NomineeStatus status) {
        ElectionNomineeDTO.builder()
                .oid(new OID())
                .nominee(UserGen.buildUser())
                .status(status)
                .build()
    }

    static List<NicheModeratorElectionDTO> buildNicheModeratorElectionList(count, ElectionStatus status) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildNicheModeratorElection(status))
        }
        res
    }

    static PageDataDTO<NicheModeratorElectionDTO> buildNicheModeratorElectionPage(Pageable pageRequest, long totalSize, ElectionStatus status) {
        PageUtil.buildPage(
                buildNicheModeratorElectionList(Math.min(totalSize, pageRequest.getPageSize()), status),
                pageRequest,
                totalSize
        )
    }

    static ElectionNomineesDTO buildElectionNominees(int count) {
        def nominees = []
        for (def i = 0; i < count; i++) {
            nominees.add(buildElectionNominee(NomineeStatus.CONFIRMED))
        }

        ElectionNomineesDTO.builder()
                .items(nominees)
                .hasMoreItems(false)
                .lastItemConfirmationDatetime(Instant.now())
                .build()
    }
}
