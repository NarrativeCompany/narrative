package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.customizations.narrative.service.api.model.ratings.AgeRatingFieldsDTO;
import org.narrative.network.customizations.narrative.service.api.model.ratings.QualityRatingFieldsDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Date: 2019-02-28
 * Time: 08:46
 *
 * @author jonmark
 */
@Mapper(config = ServiceMapperConfig.class)
public interface RatingMapper {
    @Mapping(source = "ageRatingFields.totalVoteCount", target = AgeRatingFieldsDTO.Fields.totalVoteCount)
    @Mapping(source = "ageRatingFields.scoreForApi", target = AgeRatingFieldsDTO.Fields.score)
    AgeRatingFieldsDTO mapContentToAgeRatingFieldsDTO(Content content);

    @Mapping(source = "scoreForApi", target = QualityRatingFieldsDTO.Fields.score)
    @Mapping(source = "qualityLevelForApi", target = QualityRatingFieldsDTO.Fields.qualityLevel)
    QualityRatingFieldsDTO mapQualityRatingFields(QualityRatingFields fields);
}
