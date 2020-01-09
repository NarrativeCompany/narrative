package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-01-07
 * Time: 11:36
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PostDetail")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class PostDetailDTO {
    private final OID oid;
    private final PostDTO post;

    private final String extract;
    private final String body;
    private final String canonicalUrl;
    private final boolean draft;
    private final boolean allowComments;

    private final Boolean pendingPublicationApproval;

    private final QualityRating qualityRatingByCurrentUser;
    private final AgeRating ageRatingByCurrentUser;

    private final Boolean editableByCurrentUser;
    private final Boolean deletableByCurrentUser;
}
