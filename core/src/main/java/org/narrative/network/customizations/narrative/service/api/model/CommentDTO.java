package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import org.narrative.network.customizations.narrative.service.api.model.ratings.QualityRatingFieldsDTO;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("Comment")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class CommentDTO {
    private final OID oid;
    private final String body;
    private final UserDTO user;
    private final Timestamp liveDatetime;

    private final QualityRatingFieldsDTO qualityRatingFields;

    private final QualityRating qualityRatingByCurrentUser;
}
