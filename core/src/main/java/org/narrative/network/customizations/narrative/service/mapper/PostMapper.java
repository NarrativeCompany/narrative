package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.service.api.model.EditPostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardPostDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Date: 2019-01-07
 * Time: 13:33
 *
 * @author jonmark
 */
@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class, NicheMapper.class, NicheDerivativeMapper.class, RatingMapper.class, PublicationMapper.class})
public interface PostMapper {
    // jw: we need to map the AgeRatingFields from the content object so that we can populate ageRating from Content.
    @Mapping(source = "content", target = PostDTO.Fields.ageRatingFields)
    @Mapping(source = Content.FIELD__SUBJECT__NAME, target = PostDTO.Fields.title)
    @Mapping(source = "content.contentLive", target = PostDTO.Fields.postLive)
    @Mapping(source = "futureContent.saveDatetime", target = PostDTO.Fields.lastSaveDatetime)
    PostDTO mapContentToPostDTO(Content content);

    List<PostDTO> mapContentListToPostDTOList(List<Content> contents);

    @Mapping(source = "content", target = PostDetailDTO.Fields.post)
    @Mapping(source = "compositionCache.composition.bodyResolved", target = PostDetailDTO.Fields.body)
    @Mapping(source = "compositionCache.composition.canonicalUrl", target = PostDetailDTO.Fields.canonicalUrl)
    @Mapping(source = "allowRepliesResolved", target = PostDetailDTO.Fields.allowComments)
    @Mapping(source = "extractForOgDescription", target = PostDetailDTO.Fields.extract)
    @Mapping(source = "editableByCurrentUserBoolean", target = PostDetailDTO.Fields.editableByCurrentUser)
    @Mapping(source = "deletableByCurrentUserBoolean", target = PostDetailDTO.Fields.deletableByCurrentUser)
    PostDetailDTO mapContentToPostDetailDTO(Content content);

    List<PostDetailDTO> mapContentListToPostDetailDTOList(List<Content> contents);

    @Mapping(source = "content", target = EditPostDetailDTO.Fields.postDetail)
    @Mapping(source = "content.compositionCache.composition.bodyForEdit", target = EditPostDetailDTO.Fields.rawBody)
    @Mapping(source = "content.author.personalJournal.oid", target = EditPostDetailDTO.Fields.authorPersonalJournalOid)
    @Mapping(source = "content.authorAgeRatingForEdit", target = EditPostDetailDTO.Fields.authorAgeRating)
    @Mapping(source = "content.publishedToPublication", target = EditPostDetailDTO.Fields.publishedToPublicationDetail)
    EditPostDetailDTO mapContentToEditPostDetailDTO(Content content, boolean edit);

    default RewardLeaderboardPostDTO mapContentRewardObjectPairToRewardLeaderboardPostDTO(ObjectTriplet<OID, Content, NrveValue> triplet) {
        return RewardLeaderboardPostDTO.builder()
                .postOid(triplet.getOne())
                .post(mapContentToPostDTO(triplet.getTwo()))
                .reward(new NrveUsdValue(triplet.getThree()))
                .build();
    }

    List<RewardLeaderboardPostDTO> mapContentRewardObjectPairListToRewardLeaderboardPostDTOList(List<ObjectTriplet<OID, Content, NrveValue>> triplets);
}
