package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

/**
 * Date: 2019-09-12
 * Time: 14:27
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("PublicationPowerUserInvitation")
@Value
@Builder
@FieldNameConstants
public class PublicationPowerUserInvitationDTO {
    private final OID oid;

    private final Set<PublicationRole> invitedRoles;
}
