package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.niches.nicheassociation.AssociationType;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheAssociationSlot;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

/**
 * Date: 8/27/18
 * Time: 8:44 AM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheUserAssociation")
@Value
@Builder
public class NicheUserAssociationDTO {
    private final NicheDTO niche;
    private final AssociationType type;
    private final NicheAssociationSlot associationSlot;
    private final Timestamp associationDatetime;
}
