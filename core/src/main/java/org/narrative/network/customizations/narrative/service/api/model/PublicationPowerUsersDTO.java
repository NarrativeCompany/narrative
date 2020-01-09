package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.List;
import java.util.Set;

/**
 * Date: 2019-09-12
 * Time: 14:27
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("PublicationPowerUsers")
@Value
@Builder
@FieldNameConstants
public class PublicationPowerUsersDTO {
    private final OID oid;

    private final Set<PublicationRole> currentUserCanManageRoles;
    private final Set<PublicationRole> currentUserAllowedInviteRoles;

    private final int editorLimit;
    private final int writerLimit;

    private final List<UserDTO> admins;
    private final List<UserDTO> editors;
    private final List<UserDTO> writers;

    private final List<UserDTO> invitedAdmins;
    private final List<UserDTO> invitedEditors;
    private final List<UserDTO> invitedWriters;

    // jw: we need this so that when the current user is added/removed from a role that it is reflected in the PublicationDetail
    //     object in the front end.
    private final PublicationDetailDTO publicationDetail;
}
