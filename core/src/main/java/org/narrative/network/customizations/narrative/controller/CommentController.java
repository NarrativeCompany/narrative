package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.comment.CommentInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.rating.QualityRatingInputDTO;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.CommentService;
import org.narrative.network.customizations.narrative.service.api.model.CommentDTO;
import org.narrative.network.customizations.narrative.service.api.model.CommentsDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Date: 2019-02-13
 * Time: 12:28
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/comments")
@Validated
public class CommentController {
    public static final String CONSUMER_TYPE_ID_PARAM = "consumerTypeId";
    public static final String CONSUMER_TYPE_ID_PARAMSPEC = "{"+CONSUMER_TYPE_ID_PARAM+"}";

    public static final String CONSUMER_OID_PARAM = "consumerOid";
    public static final String CONSUMER_OID_PARAMSPEC = "{"+CONSUMER_OID_PARAM+"}";

    public static final String FULL_CONSUMER_PARAMSPEC = "/"+CONSUMER_TYPE_ID_PARAMSPEC+"/"+CONSUMER_OID_PARAMSPEC;

    public static final String COMMENT_OID_PARAM = "commentOid";
    private static final String COMMENT_OID_PARAMSPEC = "{" + COMMENT_OID_PARAM + "}";

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(FULL_CONSUMER_PARAMSPEC)
    public CommentsDTO getComments(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @PageableDefault(size = 25) Pageable pageRequest, @RequestParam(required = false) boolean includeBuried, @RequestParam(name="comment", required = false) OID commentOid) {
        return commentService.getComments(consumerTypeId, consumerOid, pageRequest, includeBuried, commentOid);
    }

    @PostMapping(FULL_CONSUMER_PARAMSPEC)
    public CommentDTO postComment(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @Valid @RequestBody CommentInputDTO input) {
        return commentService.postComment(consumerTypeId, consumerOid, input);
    }

    @GetMapping(FULL_CONSUMER_PARAMSPEC + "/" + COMMENT_OID_PARAMSPEC)
    public ScalarResultDTO<String> getCommentForEdit(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @PathVariable(COMMENT_OID_PARAM) OID commentOid) {
        return commentService.getCommentForEdit(consumerTypeId, consumerOid, commentOid);
    }

    @PutMapping(FULL_CONSUMER_PARAMSPEC + "/" + COMMENT_OID_PARAMSPEC)
    public CommentDTO editComment(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @PathVariable(COMMENT_OID_PARAM) OID commentOid, @Valid @RequestBody CommentInputDTO input) {
        return commentService.editComment(consumerTypeId, consumerOid, commentOid, input);
    }

    @DeleteMapping(FULL_CONSUMER_PARAMSPEC + "/" + COMMENT_OID_PARAMSPEC)
    public void deleteComment(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @PathVariable(COMMENT_OID_PARAM) OID commentOid) {
        commentService.deleteComment(consumerTypeId, consumerOid, commentOid);
    }

    @PostMapping(FULL_CONSUMER_PARAMSPEC + "/" + COMMENT_OID_PARAMSPEC + "/quality-rating")
    public CommentDTO qualityRateComment(@PathVariable(CONSUMER_TYPE_ID_PARAM) String consumerTypeId, @PathVariable(CONSUMER_OID_PARAM) OID consumerOid, @PathVariable(COMMENT_OID_PARAM) OID commentOid, @Valid @RequestBody QualityRatingInputDTO input) {
        return commentService.qualityRateComment(consumerTypeId, consumerOid, commentOid, input.getRating(), input.getReason());
    }
}
