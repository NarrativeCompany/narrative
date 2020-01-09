package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.ContentWithAttachmentsFields;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.RatingServiceImpl;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.core.user.services.activityrate.CheckUserActivityRateLimitTask;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.posts.services.NarrativePostFields;
import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.shared.event.reputation.RatingType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-07
 * Time: 14:18
 *
 * @author jonmark
 */
public class CreateEditPostTask extends AreaTaskImpl<Content> {
    private final Content existingContent;
    private final PostInput postData;

    public CreateEditPostTask(Content existingContent, PostInput postData) {
        super(true);
        this.existingContent = existingContent;
        this.postData = postData;
    }

    @Override
    protected Content doMonitoredTask() {
        getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.POST_CONTENT);

        Partition compositionPartition;
        if (exists(existingContent)) {
            // jw: if we have an existing post, then let's make sure the current user can edit it!
            existingContent.checkEditRightForCurrentUser();

            // jw: if the author is trying to make a draft live then we need to check their activity rate limit for posting.
            if (!existingContent.isContentLive() && !postData.isDraft()) {
                // jw: let's ensure that the user has not hit their limit of new posts.
                getAreaContext().doAreaTask(new CheckUserActivityRateLimitTask(getAreaContext().getUser(), UserActivityRateLimit.POST));
            }

            compositionPartition = existingContent.getCompositionPartition();

        } else {
            compositionPartition = PartitionType.COMPOSITION.getBalancedPartition();
        }

        return getNetworkContext().doCompositionTask(compositionPartition, new CompositionTaskImpl<Content>() {
            protected Content doMonitoredTask() {

                boolean wasContentLive;
                CreateContentTask task;
                if (exists(existingContent)) {
                    task = new CreateContentTask(
                            existingContent,
                            existingContent.getComposition(),
                            OIDGenerator.getNextOID()
                    );
                    wasContentLive = existingContent.isContentLive();
                } else {
                    task = new CreateContentTask(
                            ContentType.NARRATIVE_POST,
                            getNetworkContext().getUser(),
                            getAreaContext().getPortfolio(),
                            compositionPartition,
                            OIDGenerator.getNextOID()
                    );
                    wasContentLive = false;
                }

                // jw: let's ensure that this is only being specified for new content, or content that is already
                //     a draft. Otherwise let's error out since that should never happen.
                if (postData.isDraft() && exists(existingContent) && !existingContent.isDraft()) {
                    throw UnexpectedError.getRuntimeException("Attempting to save a draft version of non-draft content. c/"+existingContent.getOid());
                }

                task.getContentFields().setDraft(postData.isDraft());

                // jw: first, let's setup the common fields.
                // jw: note: these fields are "optional" for drafts, but must have a not null value in the DB.
                task.getContentFields().setSubject(postData.getTitle() == null ? "" : postData.getTitle());
                task.getContentFields().setBody(postData.getBody() == null ? "" : postData.getBody());

                NarrativePostFields fields = (NarrativePostFields) task.getContentFields();

                if (exists(existingContent)) {
                    // bl: don't strip out attachments while in draft mode. this will ensure we don't have any issues
                    // with users cutting and pasting images with a save in the middle. that would cause the image to be
                    // removed even though the user will be able to insert it back in. safest approach is to just only
                    // delete attachments when saving a _live_ post.
                    boolean removeUnusedImages = !postData.isDraft();

                    // jw: attachments only are a concern for existing content, so let's go through the image attachments
                    //     and mark any that are not referenced in the body as "removed"
                    ContentWithAttachmentsFields attachmentFields = (ContentWithAttachmentsFields) task.getContentFields();
                    String body = attachmentFields.getBody();
                    ImageOnDisk avatarImageOnDisk = null;
                    if (!isEmptyOrNull(attachmentFields.getAttachmentList())) {
                        int lowestIndexFound = Integer.MAX_VALUE;
                        for (FileData attachment : fields.getAttachmentList()) {
                            // jw: if we hit something that is not an image let's ignore it. This way if we have another
                            //     feature down the road that adds/relies on other file types this will be non-destructive
                            //     to those.
                            if (!attachment.getFileType().isImageFile()) {
                                continue;
                            }

                            // jw: if there is nothing in the body yet, let's just go ahead and flag this as removed.
                            if (isEmpty(body)) {
                                if(removeUnusedImages) {
                                    attachment.setInclude(false);
                                }
                                continue;
                            }

                            ImageOnDisk imageOnDisk = cast(attachment.getFileOnDisk(), ImageOnDisk.class);
                            int index = body.indexOf(imageOnDisk.getPrimaryImageUrl());

                            // jw: if the image was not in the body then let's remove this image.
                            if (index < 0) {
                                if(removeUnusedImages) {
                                    attachment.setInclude(false);
                                }

                            // jw: if the image appears earlier in the body than the current one we are referencing, use it as the avatar.
                            } else if (index < lowestIndexFound) {
                                avatarImageOnDisk = imageOnDisk;
                                lowestIndexFound = index;
                            }
                        }
                    }
                    // jw: I considered setting this directly on Content, but that feels antithetical to the point of the
                    //     Fields object, so I am delegating through that.
                    fields.setAvatarImageOnDisk(avatarImageOnDisk);
                }

                // jw: let's add the Narrative Post specific data.
                fields.setSubTitle(postData.getSubTitle());
                fields.setCanonicalUrl(postData.getCanonicalUrl());
                fields.setAllowComments(!postData.isDisableComments());
                // bl: if the post channel is locked, then this indicates that the author age rating tool isn't supported
                if(!fields.isPrimaryChannelLocked()) {
                    fields.setAuthorAgeRating(postData.isAgeRestricted() ? AgeRating.RESTRICTED : AgeRating.GENERAL);
                }

                fields.setPublishToPrimaryChannel(Channel.dao().get(postData.getPublishToPrimaryChannel()));

                // jw: since the fields pre-populates the publishToNiches with the existing niches, we need to clear the
                //     list up front.
                fields.getPublishToNiches().clear();
                if (!isEmptyOrNull(postData.getPublishToNiches())) {
                    fields.getPublishToNiches().addAll(Niche.dao().getObjectsFromIDs(postData.getPublishToNiches()));
                }

                // jw: finally, we can run this task which should create the Content.
                getAreaContext().doAreaTask(task);

                Content content = task.getContent();
                // bl: once the content is live, cast an age rating vote for the author
                if(content.isContentLive()) {
                    // bl: don't allow editing of age rating via the form once the post is locked
                    if(!fields.isPrimaryChannelLocked()) {
                        new RatingServiceImpl().setRating(content, getNetworkContext().getUser(), RatingType.AGE, fields.getAuthorAgeRating(), null);
                        // jw: cache the age rating on the content so that we don't go to the database for it.
                        content.setAgeRatingByCurrentUser(fields.getAuthorAgeRating());
                    }

                    // bl: if the post is live for the first time, record a ledger entry!
                    if(!wasContentLive) {
                        getAreaContext().doAreaTask(new SaveNewPostLedgerEntry(content));
                    }
                }

                return content;
            }
        });
    }
}
