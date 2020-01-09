package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.common.web.HorizontalAlignment;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.Set;

/**
 * Date: 2019-08-01
 * Time: 15:26
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationDetail")
@Value
@FieldNameConstants
@Builder
public class PublicationDetailDTO {
    private final OID oid;
    private final PublicationDTO publication;
    private final String fathomSiteId;
    private final Instant deletionDatetime;

    private final String headerImageUrl;
    private final HorizontalAlignment headerImageAlignment;

    private final UserDTO owner;
    private final Set<PublicationRole> currentUserRoles;

    private final PublicationUrlsDTO urls;
}
