package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.postbody.posts.FeaturedPostInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.posts.PostInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.posts.PostTextInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.posts.RemovePostFromPublicationInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.rating.AgeRatingInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.rating.QualityRatingInputDTO;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.PostService;
import org.narrative.network.customizations.narrative.service.api.model.EditPostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ImageAttachmentDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDetailDTO;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * Date: 2019-01-04
 * Time: 14:43
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/posts")
@Validated
public class PostController {
    public static final String POST_OID_PARAM = "postOid";
    private static final String POST_OID_PARAMSPEC = "{" + POST_OID_PARAM + "}";

    public static final String POST_ID_PARAM = "postId";
    private static final String POST_ID_PARAMSPEC = "{" + POST_ID_PARAM + "}";

    private static final String NICHE_OID_PARAM = "nicheOid";
    private static final String NICHE_OID_PARAMSPEC = "{" + NICHE_OID_PARAM + "}";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/validate-text")
    public EditPostDetailDTO validatePostText(@Valid @RequestBody PostTextInputDTO textData) {
        // jw: Since the only purpose of this is to validate the post data, we can just short out.
        return null;
    }

    @PostMapping
    public EditPostDetailDTO submitPost(@Valid @RequestBody PostInputDTO postData) {
        return postService.submitPost(null, postData);
    }

    @PostMapping("/" + POST_OID_PARAMSPEC)
    public EditPostDetailDTO editPost(@PathVariable(POST_OID_PARAM) OID postOid, @Valid @RequestBody PostInputDTO postData) {
        return postService.submitPost(postOid, postData);
    }

    @GetMapping("/" + POST_ID_PARAMSPEC)
    public PostDetailDTO getPost(@PathVariable(POST_ID_PARAM) String postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/" + POST_OID_PARAMSPEC + "/edit-detail")
    public EditPostDetailDTO getPostForEdit(@PathVariable(POST_OID_PARAM) OID postOid) {
        return postService.getPostForEdit(postOid);
    }

    @PutMapping("/" + POST_OID_PARAMSPEC + "/featured")
    public PostDetailDTO featurePost(@PathVariable(POST_OID_PARAM) OID postOid, @Valid @RequestBody FeaturedPostInputDTO featurePostInput) {
        return postService.featurePost(postOid, featurePostInput.getDuration());
    }

    @DeleteMapping("/" + POST_OID_PARAMSPEC + "/featured")
    public PostDetailDTO unfeaturePost(@PathVariable(POST_OID_PARAM) OID postOid) {
        return postService.unfeaturePost(postOid);
    }

    @GetMapping("/" + POST_OID_PARAMSPEC + "/all-time-rewards")
    public ScalarResultDTO<NrveUsdValue> getPostAllTimeRewards(@PathVariable(POST_OID_PARAM) OID postOid) {
        return postService.getPostAllTimeRewards(postOid);
    }

    @DeleteMapping("/" + POST_OID_PARAMSPEC)
    public void deletePost(@PathVariable(POST_OID_PARAM) OID postOid) {
        postService.deletePost(postOid);
    }

    @PostMapping("/" + POST_OID_PARAMSPEC + "/attachments")
    public ImageAttachmentDTO uploadPostAttachment(@PathVariable(POST_OID_PARAM) OID postOid, @RequestParam("file") MultipartFile file) {
        return postService.uploadPostAttachment(postOid, file);
    }

    @PostMapping("/" + POST_OID_PARAMSPEC + "/quality-rating")
    public PostDetailDTO qualityRatePost(@PathVariable(POST_OID_PARAM) OID postOid, @Valid @RequestBody QualityRatingInputDTO input) {
        return postService.qualityRatePost(postOid, input.getRating(), input.getReason());
    }

    @PostMapping("/" + POST_OID_PARAMSPEC + "/age-rating")
    public PostDetailDTO ageRatePost(@PathVariable(POST_OID_PARAM) OID postOid, @Valid @RequestBody AgeRatingInputDTO input) {
        return postService.ageRatePost(postOid, input.getRating());
    }

    @DeleteMapping("/" + POST_OID_PARAMSPEC + "/niches/" + NICHE_OID_PARAMSPEC)
    public PostDetailDTO removePostFromNiche(@PathVariable(POST_OID_PARAM) OID postOid, @PathVariable(NICHE_OID_PARAM) OID nicheOid) {
        return postService.removePostFromNiche(postOid, nicheOid);
    }

    @PutMapping("/" + POST_OID_PARAMSPEC + "/publication")
    public PostDetailDTO approvePostInPublication(@PathVariable(POST_OID_PARAM) OID postOid) {
        return postService.approvePostInPublication(postOid);
    }

    @PostMapping("/" + POST_OID_PARAMSPEC + "/publication/delete")
    public PostDetailDTO removePostFromPublication(@PathVariable(POST_OID_PARAM) OID postOid, @Valid @RequestBody RemovePostFromPublicationInputDTO input) {
        return postService.removePostFromPublication(postOid, input.getMessage());
    }
}
