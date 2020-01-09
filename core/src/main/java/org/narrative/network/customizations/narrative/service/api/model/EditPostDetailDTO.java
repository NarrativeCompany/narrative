package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.Collection;
import java.util.List;

/**
 * Date: 2019-01-10
 * Time: 11:04
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("EditPostDetail")
@Value
@FieldNameConstants
@Builder
public class EditPostDetailDTO {
    private final PostDetailDTO postDetail;
    /**
     * store the rawBody separately from the PostDetail.body, which has post-processing done to it
     */
    private final String rawBody;

    private final AgeRating authorAgeRating;

    private final boolean edit;

    private final OID authorPersonalJournalOid;
    private final List<PublicationDTO> availablePublications;
    private final Collection<OID> blockedInNicheOids;

    private final PublicationDetailDTO publishedToPublicationDetail;
}
