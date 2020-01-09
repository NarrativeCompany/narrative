package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-09-24
 * Time: 10:59
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("SearchResult")
@Value
@Builder
@FieldNameConstants
public class SearchResultDTO {
    private final OID oid;

    // jw: the rest of these are all optional, though one should always be present.
    private final UserDetailDTO userDetail;
    private final NicheDTO niche;
    private final PublicationDTO publication;
    private final PostDTO post;
}
