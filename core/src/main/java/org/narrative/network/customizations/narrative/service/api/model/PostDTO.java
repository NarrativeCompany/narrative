package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import org.narrative.network.customizations.narrative.service.api.model.ratings.AgeRatingFieldsDTO;
import org.narrative.network.customizations.narrative.service.api.model.ratings.QualityRatingFieldsDTO;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

/**
 * Date: 2019-01-07
 * Time: 11:37
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("Post")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class PostDTO {
    private final OID oid;
    private final String title;
    private final String subTitle;
    private final UserDTO author;
    private final String prettyUrlString;

    private final boolean postLive;

    private final Timestamp moderationDatetime;

    private final Timestamp liveDatetime;
    private final Timestamp lastUpdateDatetime;
    private final Timestamp lastSaveDatetime;

    private final String titleImageUrl;
    private final String titleImageLargeUrl;
    private final Integer titleImageLargeWidth;
    private final Integer titleImageLargeHeight;
    private final String titleImageSquareUrl;

    private final QualityRatingFieldsDTO qualityRatingFields;
    private final AgeRatingFieldsDTO ageRatingFields;

    private final boolean publishedToPersonalJournal;
    private final PublicationDTO publishedToPublication;
    private final Collection<NicheDTO> publishedToNiches;

    private final Boolean featuredInPublication;
    private final Instant featuredUntilDatetime;
}
