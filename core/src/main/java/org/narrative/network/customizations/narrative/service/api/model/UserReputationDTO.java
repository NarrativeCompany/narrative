package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.reputation.ReputationLevel;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Date: 2018-12-13
 * Time: 08:39
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("UserReputation")
@Value
@Builder(toBuilder = true)
public class UserReputationDTO {
    private final OID oid;
    private final boolean isConductNegative;
    private final Instant negativeConductExpirationTimestamp;
    private final int qualityAnalysisScore;
    private final int kycVerifiedScore;
    private final Boolean kycVerificationPending;
    private final int totalScore;
    private final ReputationLevel level;
}
