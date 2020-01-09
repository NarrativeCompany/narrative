package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.posts.FeaturePostDuration;
import org.narrative.network.customizations.narrative.service.api.model.EditPostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ImageAttachmentDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;
import org.springframework.web.multipart.MultipartFile;

/**
 * Date: 2019-01-07
 * Time: 13:10
 *
 * @author jonmark
 */
public interface PostService {
    EditPostDetailDTO submitPost(OID postOid, PostInput postData);

    void deletePost(OID postOid);

    PostDetailDTO getPost(String postid);

    EditPostDetailDTO getPostForEdit(OID postOid);

    ScalarResultDTO<NrveUsdValue> getPostAllTimeRewards(OID postOid);

    ImageAttachmentDTO uploadPostAttachment(OID postOid, MultipartFile file);

    PostDetailDTO qualityRatePost(OID postOid, QualityRating rating, String reason);

    PostDetailDTO ageRatePost(OID postOid, AgeRating rating);

    PostDetailDTO removePostFromNiche(OID postOid, OID nicheOid);

    PostDetailDTO featurePost(OID postOid, FeaturePostDuration duration);

    PostDetailDTO unfeaturePost(OID postOid);

    PostDetailDTO approvePostInPublication(OID postOid);

    PostDetailDTO removePostFromPublication(OID postOid, String message);
}
