package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.CommentDTO;
import org.narrative.network.customizations.narrative.service.api.model.CommentsDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CommentInput;
import org.springframework.data.domain.Pageable;

/**
 * Date: 2019-02-13
 * Time: 12:29
 *
 * @author jonmark
 */
public interface CommentService {
    CommentsDTO getComments(String consumerTypeId, OID consumerOid, Pageable pageRequest, boolean includeBuried, OID commentOid);

    CommentDTO postComment(String consumerTypeId, OID consumerOid, CommentInput input);

    CommentDTO editComment(String consumerTypeId, OID consumerOid, OID commentOid, CommentInput input);

    ScalarResultDTO<String> getCommentForEdit(String consumerTypeId, OID consumerOid, OID commentOid);

    void deleteComment(String consumerTypeId, OID consumerOid, OID commentOid);

    CommentDTO qualityRateComment(String consumerTypeId, OID consumerOid, OID commentOid, QualityRating rating, String reason);
}
