package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.PageNotFoundError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.RoleContentPageView;
import org.narrative.network.core.area.base.services.ItemHourTrendingStatsManager;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.narrative.rewards.ContentReward;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.RatingService;
import org.narrative.network.core.rating.RatingValue;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.network.core.search.services.ContentIndexRunnable;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.core.user.services.activityrate.CheckUserActivityRateLimitTask;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.PostController;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.FeaturePostDuration;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.posts.services.SendPostApprovedInChannelEmailTask;
import org.narrative.network.customizations.narrative.posts.services.SendPostRemovedFromChannelEmailTask;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.NicheService;
import org.narrative.network.customizations.narrative.service.api.PostService;
import org.narrative.network.customizations.narrative.service.api.model.EditPostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ImageAttachmentDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;
import org.narrative.network.customizations.narrative.service.api.services.ParseObjectFromUnknownIdTask;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.apache.commons.codec.digest.DigestUtils;
import org.narrative.shared.event.reputation.RatingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.Collections;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-01-07
 * Time: 13:13
 *
 * @author jonmark
 */
@Service
@Transactional
public class PostServiceImpl implements PostService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final PostMapper postMapper;
    private final RatingService ratingService;
    private final NicheService nicheService;

    public PostServiceImpl(
            AreaTaskExecutor areaTaskExecutor,
            PostMapper postMapper,
            RatingService ratingService,
            NicheService nicheService) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.postMapper = postMapper;
        this.ratingService = ratingService;
        this.nicheService = nicheService;
    }

    @Override
    public EditPostDetailDTO submitPost(OID postOid, PostInput postData) {
        Content content = postOid == null ? null : Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);
        boolean isEdit = exists(content) && !content.isDraft();

        content = areaTaskExecutor.executeAreaTask(new CreateEditPostTask(content, postData));

        return getEditPostDetailDTO(content, isEdit);
    }

    @Override
    public void deletePost(OID postOid) {
        areaTaskExecutor.executeAreaTask(new DeletePostTask(postOid));
    }

    @Override
    public PostDetailDTO getPost(String postId) {
        Content content = getContentFromUnknownPostId(postId);

        if(!exists(content)) {
            // bl: we need to throw an error so that the request returns a proper 404. previously this was returning
            // null which resulted in an empty response with a 200 result, which is wrong
            throw new PageNotFoundError(wordlet("pageNotFound.postNotFound"));
        }

        // jw: if this is not a pre-render agent, then let's record a view!
        if (!networkContext().getReqResp().getClientAgentInformation().getClientAgentType().isPrerender() && content.isContentLive()) {
            // bl: if this role has viewed in the last day, then we aren't going to count it again.
            // for guests, we use a hash of their IP address
            String roleId;
            if(networkContext().getPrimaryRole().isRegisteredUser()) {
                roleId = networkContext().getUser().getOid().toString();
            } else {
                // bl: use a SHA512 hash for IP addresses so we don't actually store the IP addresses themselves.
                roleId = DigestUtils.sha512Hex(networkContext().getReqResp().getRemoteHostIp());
            }
            if(!RoleContentPageView.dao().hasRoleViewedRecently(roleId, content.getOid())) {
                ItemHourTrendingStatsManager.recordContentView(networkContext().getPrimaryRole(), content);
                // bl: we also need to record the view. save it for end of PartitionGroup.
                // note that this technically introduces a race condition. if a user/IP makes multiple requests
                // very quickly, they would each potentially record the view before this record gets inserted.
                // once one record has been inserted, though, no future views will count until 24 hours have passed.
                PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        RoleContentPageView.dao().save(new RoleContentPageView(roleId, content.getOid()));
                        return null;
                    }
                }));
            }
        }

        // bl: include the Publication for moderated content for Publication editors
        if (content.isManageableByCurrentUser()) {
            content.setIncludePublicationForModeratedContent(true);
        }

        return getPostDetailDTO(content);
    }

    private Content getContentFromUnknownPostId(String postId) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Content>(false) {
            @Override
            protected Content doMonitoredTask() {
                Content content = areaTaskExecutor.executeAreaTask(new ParseObjectFromUnknownIdTask<Content>(postId, PostController.POST_ID_PARAM) {
                    @Override
                    protected Content getFromOid(OID oid) {
                        return Content.dao().get(oid);
                    }

                    @Override
                    protected Content getFromPrettyUrlString(String prettyUrlString) {
                        return Content.dao().getForPrettyURLString(
                                getAreaContext().getPortfolio(),
                                ContentType.NARRATIVE_POST,
                                prettyUrlString
                        );
                    }
                });

                // jw: if we did not find a post, let's just return null and allow the front end to display the "NotFound" component.
                // jw: similarly, if the content is a draft, let's treat it the same way.
                // bl: intentionally excluding moderation/live checks here since the checkViewRight below should handle those
                if (!exists(content) || content.isDraft()) {
                    return null;
                }

                // jw: ensure that we were given a Narrative Post.
                if (!content.getContentType().isNarrativePost()) {
                    throw UnexpectedError.getRuntimeException("Should only ever be provided a Narrative Post!");
                }

                // jw: let's check general view right on the content.
                content.checkViewRight(getAreaContext().getAreaRole());

                return content;
            }
        });
    }

    @Override
    public EditPostDetailDTO getPostForEdit(OID postOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<EditPostDetailDTO>(false) {
            @Override
            protected EditPostDetailDTO doMonitoredTask() {
                Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

                // jw: since this is for editing purposes, let's ensure that the viewer has the right to edit it.
                content.checkEditRightForCurrentUser();
                getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.POST_CONTENT);

                return getEditPostDetailDTO(content, false);
            }
        });
    }

    private EditPostDetailDTO getEditPostDetailDTO(Content content, boolean isEdit) {
        if (!exists(content)) {
            throw UnexpectedError.getRuntimeException("Should always have a Narrative Post at this point!");
        }

        if (!content.getContentType().isNarrativePost()) {
            throw UnexpectedError.getRuntimeException("Should only ever be provided a Narrative Post!");
        }

        // jw: do not allow access to content that is published to a publication unless it is a draft. In that case
        //     we will not include the publication in the options for the selector, so it should naturally clear itself
        //     out.
        ChannelContent primaryChannelContent = content.getPrimaryChannelContent();
        if (exists(primaryChannelContent) && (content.isLive() || content.isModerated())) {
            Channel channel = primaryChannelContent.getChannel();

            // jw: now, let's just see if it's expired.
            if (channel.getType().isPublication()) {
                channel.getPublication().assertNotExpired(false);
            }
        }

        // jw: we need to prime the CompositionCache
        Composition.loadCompositions(Collections.singletonList(content), true);

        // jw: we need to initialize the followedByCurrentUser on all niches that this post was published to.
        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(networkContext().getUser(), content.getPublishedToChannels());

        // jw: because we lazily initialize objects from the Composition partition, we need to make sure that is in scope here.
        return areaTaskExecutor.executeCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<EditPostDetailDTO>(false) {
            @Override
            protected EditPostDetailDTO doMonitoredTask() {
                // bl: we need to always include the Publication here, even if it's pending publication approval
                content.setIncludePublicationForModeratedContent(true);
                return postMapper.mapContentToEditPostDetailDTO(content, isEdit);
            }
        });
    }

    @Override
    public ScalarResultDTO<NrveUsdValue> getPostAllTimeRewards(OID postOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ScalarResultDTO<NrveUsdValue>>(false) {
            @Override
            protected ScalarResultDTO<NrveUsdValue> doMonitoredTask() {
                Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

                NrveValue allTimeRewards = ContentReward.dao().getAllTimeRewardsForContent(content);
                return ScalarResultDTO.<NrveUsdValue>builder()
                        .value(new NrveUsdValue(allTimeRewards))
                        .build();
            }
        });
    }

    @Override
    public ImageAttachmentDTO uploadPostAttachment(OID postOid, MultipartFile file) {
        ImageOnDisk imageOnDisk = areaTaskExecutor.executeAreaTask(new UploadPostAttachmentTask(postOid, file));

        // jw: need to return an object that provides the image URL
        return ImageAttachmentDTO.builder()
                .link(imageOnDisk.getPrimaryImageUrl())
                .build();
    }

    @Override
    public PostDetailDTO ageRatePost(OID postOid, AgeRating rating) {
        Content content = doRatingTask(postOid, RatingType.AGE, rating, null);

        // jw: be sure to cache this rating on the content, there is no need to go to the DB for it.
        content.setAgeRatingByCurrentUser(rating);

        return getPostDetailDTO(content);
    }

    @Override
    public PostDetailDTO qualityRatePost(OID postOid, QualityRating rating, String reason) {
        Content content = doRatingTask(postOid, RatingType.QUALITY, rating, reason);

        // jw: be sure to cache this rating on the content, there is no need to go to the DB for it.
        content.setQualityRatingByCurrentUser(rating);

        return getPostDetailDTO(content);
    }

    @Override
    public PostDetailDTO removePostFromNiche(OID postOid, OID nicheOid) {
        Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

        Niche niche = nicheService.getNicheFromUnknownId(nicheOid.toString());

        // bl: make sure that the current user is a moderator for the niche
        networkContext().getPrimaryRole().checkNicheModerator(niche);

        removePostFromChannel(content, niche.getChannel(), false, null);

        return getPostDetailDTO(content);
    }

    private void removePostFromChannel(Content content, Channel channel, boolean forModeration, String message) {
        ChannelContent channelContent = content.getChannelContentsInited().get(channel);
        // bl: if the post isn't in the channel, then that's an error.
        if(!exists(channelContent)) {
            throw UnexpectedError.getRuntimeException("Should never try to remove a post from a channel that it's not in!");
        }

        NarrativePostStatus originalStatus = channelContent.getStatus();

        if(channel.getType().isBlockRemovedPosts()) {
            // bl: once the post is removed from a niche, it's permanently, irrevocably blocked from being in the niche again.
            channelContent.setStatus(NarrativePostStatus.BLOCKED);
        } else {
            // bl: in publications, we just need to remove the ChannelContent
            content.getChannelContentsInited().remove(channel);
            ChannelContent.dao().delete(channelContent);
        }

        // jw: if the content was active in the channel, then we need to trigger a re-index of the content.
        if (originalStatus.isApproved()) {
            ContentIndexRunnable.registerContentIndexRunnable(content);
        }

        // bl: record a ledger entry for the event
        // bl: don't record a ledger entry if it's for moderation
        if(!forModeration) {
            LedgerEntry ledgerEntry = new LedgerEntry(areaContext().getAreaUserRlm(), LedgerEntryType.POST_REMOVED_FROM_CHANNEL);
            ledgerEntry.setChannel(channel);
            ledgerEntry.setContentOid(content.getOid());
            networkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
        }

        // bl: finally we must send an email to the user to let them know that their post was removed from the channel
        areaTaskExecutor.executeAreaTask(new SendPostRemovedFromChannelEmailTask(content, channel, false, networkContext().getUser(), forModeration, message));
    }

    // jw: there is quite a bit of setup that needs to happen for PostDetailDTO, so let's centralize that here.
    private PostDetailDTO getPostDetailDTO(Content content) {
        // jw: let's initialize the Composition now, since we will need it for the DTO.
        Composition.loadCompositions(Collections.singletonList(content), true);

        // jw: we need to initialize the followedByCurrentUser on all niches that this post was published to.
        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(networkContext().getUser(), content.getPublishedToChannels());

        // jw: because we lazily initialize objects from the Composition partition, we need to make sure that is in scope here.
        return areaTaskExecutor.executeCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<PostDetailDTO>(false) {
            @Override
            protected PostDetailDTO doMonitoredTask() {
                return postMapper.mapContentToPostDetailDTO(content);
            }
        });
    }

    private <RV extends RatingValue<URO>,
            URO extends UserRatedObject<DAO,RV>,
            DAO extends UserRatedObjectDAO<Content, URO, OID, RV>>
    Content doRatingTask(OID postOid, RatingType ratingType, RV rating, String reason) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Content>() {
            @Override
            protected Content doMonitoredTask() {
                // jw: ensure the user has the right to rate content!
                getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.RATE_CONTENT);

                // jw: ensure that the user has not rated more content today than they are allowed.
                getAreaContext().doAreaTask(new CheckUserActivityRateLimitTask(getAreaContext().getUser(), UserActivityRateLimit.RATE_POST));

                Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

                // jw: before we process any kind of rating, let's make sure this person can view the post
                content.checkViewRight(getAreaContext().getAreaRole());

                // bl: if the post isn't live, then you can't rate it!
                if(!content.isContentLive()) {
                    throw UnexpectedError.getRuntimeException("Shouldn't attempt to rate a post before it's live!");
                }

                boolean authorUpdateAgeRating = false;
                // jw: finally, let's ensure that the author is not trying to rate their own post.
                if(exists(content.getAuthor()) && content.getAuthor().isCurrentUserThisUser()) {
                    // bl: the author can now age rate their posts once the primary channel is locked (e.g. submitted to a publication).
                    // in that case, we have to let it through and set the author's age rating directly on the post.
                    if(ratingType.isAge() && content.isPrimaryChannelLocked()) {
                        authorUpdateAgeRating = true;
                    } else {
                        throw UnexpectedError.getRuntimeException("Can't rate your own posts!");
                    }
                }

                PartitionType.COMPOSITION.doTask(content.getCompositionPartition(), new TaskOptions(), new CompositionTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        ratingService.setRating(content, getNetworkContext().getUser(), ratingType, rating, reason);
                        return null;
                    }
                });

                // bl: have to updateAuthorAgeRating after we set the rating above because it locks on the Content, so we can't have dirty props before doing the refresh
                if(authorUpdateAgeRating) {
                    // bl: the double-cast here is necessary in order to further erase types to keep the compiler
                    // from breaking with a StackOverflowError. super annoying to track down why the code wouldn't compile.
                    // clearly has something to do with the parameterized types on this method.
                    content.updateAuthorAgeRating((AgeRating)(RatingValue)rating);
                }

                return content;
            }
        });
    }

    @Override
    public PostDetailDTO featurePost(OID postOid, FeaturePostDuration duration) {
        return doManagePublicationPostTask(postOid, true, (content, publication) -> {
            // bl: if the post doesn't have an image, then it can't be featured
            if(!content.isHasTitleImage()) {
                throw new ApplicationError(wordlet("error.cantFeaturePublicationPost.noImage"));
            }
            // bl: just feature the post!
            content.getPrimaryChannelContent().featurePost(duration);
        });
    }

    @Override
    public PostDetailDTO unfeaturePost(OID postOid) {
        return doManagePublicationPostTask(postOid, true, (content, publication) -> {
            // bl: just unfeature the post! note that this works even if the post isn't currently featured.
            content.getPrimaryChannelContent().unfeaturePost();
        });
    }

    @Override
    public PostDetailDTO approvePostInPublication(OID postOid) {
        return doManagePublicationPostTask(postOid, false, (content, publication) -> {
            // approval is only necessary when the post is actually moderated
            if(!content.getPrimaryChannelContent().getStatus().isModerated()) {
                return;
            }

            boolean wasContentLive = content.isContentLive();

            // bl: just set the moderationStatus and delete ModeratedContent!
            content.approveContent();

            if(!wasContentLive) {
                // bl: also update the liveDatetime since now is the first time the post is going live!
                content.updateLiveDatetime(new Timestamp(System.currentTimeMillis()));

                areaTaskExecutor.executeAreaTask(new SaveNewPostLedgerEntry(content));
            }

            // jw: now that the post is active in the publication we need to re-index the content so it will appear in
            //     the publication post search.
            ContentIndexRunnable.registerContentIndexRunnable(content);

            // bl: finally we must send an email to the author to let them know that their post was approved in the publication
            areaTaskExecutor.executeAreaTask(new SendPostApprovedInChannelEmailTask(content, publication.getChannel(), networkContext().getUser()));
        });
    }

    @Override
    public PostDetailDTO removePostFromPublication(OID postOid, String message) {
        return doManagePublicationPostTask(postOid, false, (content, publication) -> {
            CleanupPostForPublicationRemovalTask cleanupTask = new CleanupPostForPublicationRemovalTask(content);
            areaTaskExecutor.executeAreaTask(cleanupTask);

            // bl: do the common stuff for removing posts from a channel.
            // will record a ledger entry (if applicable) and send the author an email
            removePostFromChannel(content, publication.getChannel(), cleanupTask.isWasModerated(), message);
        });
    }

    private PostDetailDTO doManagePublicationPostTask(OID postOid, boolean requiresPostLiveInPublication, ManagePublicationPostTask task) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<PostDetailDTO>() {
            @Override
            protected PostDetailDTO doMonitoredTask() {
                Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);

                Publication publication = content.getSubmittedToPublication();

                if(!exists(publication)) {
                    throw UnexpectedError.getRuntimeException("Should not attempt to manage a post not in a Publication!");
                }

                // bl: only publication editors should pass this permission check
                content.checkManageable(getAreaContext().getAreaRole());

                // bl: if this check requires that the post is live in the publication, then do the extra enforcement
                if(requiresPostLiveInPublication && !content.getPrimaryChannelContent().getStatus().isApproved()) {
                    throw UnexpectedError.getRuntimeException("Shouldn't use this endpoint before the post has been approved in the Publication!");
                }

                task.doTask(content, publication);

                return getPostDetailDTO(content);
            }
        });
    }

    private interface ManagePublicationPostTask {
        void doTask(Content content, Publication publication);
    }
}
