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
 * Date: 2019-09-25
 * Time: 11:42
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationUserAssociation")
@Value
@Builder
@FieldNameConstants
public class PublicationUserAssociationDTO {
    // jw: this will correspond to the publication, and is here for apollo caching purposes.
    private final OID oid;

    private final PublicationDTO publication;
    private final Set<PublicationRole> roles;
    private final boolean owner;
}
