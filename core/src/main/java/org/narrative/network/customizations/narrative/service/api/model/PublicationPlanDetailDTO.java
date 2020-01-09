package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.Set;

/**
 * Date: 2019-08-14
 * Time: 13:46
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationPlanDetail")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PublicationPlanDetailDTO {
    private final OID oid;
    private final PublicationPlanType plan;
    private final boolean withinTrialPeriod;
    private final boolean withinRenewalPeriod;
    private final boolean eligibleForDiscount;
    private final Instant endDatetime;
    private final Instant deletionDatetime;

    private final int admins;
    private final int editors;
    private final int writers;

    private final Set<PublicationPlanType> renewalPlans;
    private final Set<PublicationPlanType> upgradePlans;
}
