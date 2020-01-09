package org.narrative.reputation.util.datagen


import org.apache.commons.lang3.RandomUtils
import org.narrative.reputation.model.entity.CurrentReputationEntity
import spock.lang.Specification

import java.time.Instant

/**
 * Test data generator for {@link CurrentReputationEntity}
 */
class CurRepGen extends Specification {
        static CurrentReputationEntity buildCurrentRepEntity(long userOid,
                                                       UUID lastEventId,
                                                       Double qualityAnalysis,
                                                       Boolean kycVerified) {
        int qualityAnalysisNew = qualityAnalysis == null ? RandomUtils.nextInt(1, 100) : qualityAnalysis
            boolean kycVerifiedNew = kycVerified == null ? RandomUtils.nextBoolean() : kycVerified

            def qas = (double) 0.4 * qualityAnalysisNew
        def kyc = kycVerifiedNew ? (double) 0.2 : 0

        return CurrentReputationEntity.builder()
                .userOid(userOid)
                .lastUpdated(Instant.now())
                .lastEventId(lastEventId == null ? UUID.randomUUID() : lastEventId)
                .qualityAnalysis(qualityAnalysisNew)
                .kycVerified(kycVerifiedNew)
                .totalScore(Math.floor(qas + kyc).intValue())
                .build();

    }

    static List<CurrentReputationEntity> buildCurrentRepList(count, startIndex = 1) {
        def res = []
        for (def i = 0; i < count; i++) {
            res.add(buildCurrentRepEntity(i + startIndex, null, null,  null))
        }
        res
    }

}