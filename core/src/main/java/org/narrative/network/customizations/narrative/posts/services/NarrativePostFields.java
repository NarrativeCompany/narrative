package org.narrative.network.customizations.narrative.posts.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UrlUtil;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.ContentWithAttachmentsFields;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.moderation.ModeratedContent;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostContent;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationSettings;
import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;
import org.narrative.network.customizations.narrative.service.api.model.validators.ValidPostInputValidator;
import org.narrative.network.customizations.narrative.service.impl.publication.SendNewPostNotificationToPublicationEditorsEmailTask;
import org.narrative.network.shared.security.AccessViolation;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-01-03
 * Time: 13:50
 *
 * @author jonmark
 */
@FieldNameConstants
public class NarrativePostFields extends ContentWithAttachmentsFields<NarrativePostContent> {
    public static final int MAX_NICHES = 3;

    private String subTitle;
    private String canonicalUrl;
    private Channel publishToPrimaryChannel;
    private boolean primaryChannelLocked;
    private boolean allowComments;
    private AgeRating authorAgeRating;
    private ImageOnDisk avatarImageOnDisk;

    private final Set<Niche> publishToNiches = new HashSet<>();
    private final Set<Channel> blockedInChannels = new HashSet<>();
    private final Set<Niche> originalNiches = new HashSet<>();

    public NarrativePostFields(User user, OID fileUploadProcessOid) {
        super(user, fileUploadProcessOid);
        // bl: no longer posting to the personal journal by default, so leave this unset
        //publishToPrimaryChannel = user.getPersonalJournal().getChannel();
        allowComments = true;
        authorAgeRating = AgeRating.GENERAL;
    }

    public NarrativePostFields(Content content, NarrativePostContent post, OID fileUploadProcessOid) {
        super(content, post, fileUploadProcessOid);
        this.allowComments = content.isAllowReplies();
        this.subTitle = content.getSubTitle();
        this.canonicalUrl = post.getComposition().getCanonicalUrl();
        this.authorAgeRating = content.getAuthorAgeRating();
        this.avatarImageOnDisk = content.getAvatarImageOnDisk();

        // jw: let's sort through the places this was published and update the fields accordingly.
        for (ChannelContent channelContent : content.getChannelContentsInited().values()) {
            Channel channel = channelContent.getChannel();

            if (channelContent.getStatus().isBlocked()) {
                blockedInChannels.add(channel);

            } else if (channel.getType().isNiche()) {
                publishToNiches.add(channel.getNiche());

            } else {
                assert channel.getType().isPrimaryPublishingChannel() : "Expected a primary publishing Channel.";
                assert publishToPrimaryChannel==null : "Should never have multiple primary channels set!";
                publishToPrimaryChannel = channel;
                primaryChannelLocked = content.isPrimaryChannelLocked();
            }
        }

        // jw: Let's keep a backup list of the publishToNiches so that we can track these later and properly account for
        //     for them during validation.
        originalNiches.addAll(publishToNiches);
    }

    @Override
    public void validateSubject(CreateContentTask task) {
        // jw: only validate the subject if this is not a draft, or the subject is specified
        if (!isDraft() || !isEmpty(getSubject())) {
            super.validateSubject(task);
        }
    }

    @Override
    public boolean isBodyRequired() {
        // jw: this one is a bit more tricky. Let's validate the body as long as this is either not a draft, or a body is specified.
        return !isDraft() || !isEmpty(getBody());
    }

    @Override
    public void validate(CreateContentTask task) {
        super.validate(task);

        // jw: the subtitle is optional, so only validate it if we have a value for it.
        if (!isEmpty(getSubTitle())) {
            task.getValidationHandler().validateString(getSubTitle(), getMinSubjectLength(), getMaxSubjectLength(), Fields.subTitle, "narrativePostFields.subTitle");
        }

        if (!isEmpty(getCanonicalUrl())) {
            // bl: when saving a draft, don't record a validation error for canonical URL. instead, just don't
            // save the value for it.
            if(isDraft()) {
                int urlLength = getCanonicalUrl().length();
                if(urlLength<Composition.MIN_CANONICAL_URL_LENGTH || urlLength>Composition.MAX_CANONICAL_URL_LENGTH || !UrlUtil.isUrlValid(getCanonicalUrl())) {
                    setCanonicalUrl(null);
                }
            } else {
                if(task.getValidationHandler().validateString(getCanonicalUrl(), Composition.MIN_CANONICAL_URL_LENGTH, Composition.MAX_CANONICAL_URL_LENGTH, Fields.canonicalUrl, "narrativePostFields.canonicalUrl")) {
                    if(!UrlUtil.isUrlValid(getCanonicalUrl())) {
                        task.getValidationHandler().addWordletizedFieldError(Fields.canonicalUrl, "narrativePostFields.canonicalUrlLengthErrorArgs", Composition.MIN_CANONICAL_URL_LENGTH, Composition.MAX_CANONICAL_URL_LENGTH);
                    }
                }
            }
        }

        if (publishToNiches.isEmpty()) {
            if (!exists(publishToPrimaryChannel) && !isDraft()) {
                task.getValidationHandler().addActionError(wordlet("narrativePostFields.mustPublishToAtLeastOnePlace"));
            }

        } else {
            // jw: before we validate the niche count, let's ensure they are not trying to associate this with a niche it has been blocked in.
            Iterator<Niche> nicheIterator = publishToNiches.iterator();
            while (nicheIterator.hasNext()) {
                Niche niche = nicheIterator.next();

                if (blockedInChannels.contains(niche.getChannel())) {
                    nicheIterator.remove();

                // jw: if the niche was not originally associated to this content then we need to ensure that it is active.
                } else if (!niche.getStatus().isActive() && !originalNiches.contains(niche)) {
                    task.getValidationHandler().addFieldError(
                            PostInput.Fields.publishToNiches,
                            wordlet("narrativePostFields.specifiedNonActiveNiche", niche.getName())
                    );
                }
            }

            // jw: now that we have cleaned the publication list up, let's ensure that they are not trying to associate this
            //     with too many niches.
            if (publishToNiches.size() > MAX_NICHES) {
                task.getValidationHandler().addFieldError(PostInput.Fields.publishToNiches, wordlet("narrativePostFields.cannotPublishToSoManyNiches", formatNumber(MAX_NICHES)));
            }
        }

        // bl: if the primary channel is locked, then override any supplied value here to force it to stay
        if(primaryChannelLocked) {
            publishToPrimaryChannel = getCurrentContent().getPrimaryChannelContent().getChannel();
        } else if(exists(publishToPrimaryChannel)) {
            // bl: make sure the user actually has permission to post to this channel
            if(!publishToPrimaryChannel.getConsumer().isCanCurrentRolePost()) {
                throw new AccessViolation(wordlet("channelPosting.accessViolation", publishToPrimaryChannel.getType().getNameForDisplay()));
            }
            // bl: make sure the post hasn't been blocked from this channel
            Content currentContent = getCurrentContent();
            if(exists(currentContent) && currentContent.isBlockedInChannel(publishToPrimaryChannel)) {
                throw new AccessViolation(wordlet("channelPostingBlocked.accessViolation", publishToPrimaryChannel.getType().getNameForDisplay()));
            }
        }

        // jw: we should allow neither editing or posting of content to expired publications, so let's check that here as
        //     its own thing.
        if (exists(publishToPrimaryChannel) && publishToPrimaryChannel.getType().isPublication() && !isDraft()) {
            publishToPrimaryChannel.getPublication().assertNotExpired(false);
        }
    }

    public void execute(Content content) {
        // jw: first, let's handle the simple fields.
        content.setSubTitle(subTitle);
        content.getComposition().setCanonicalUrl(isEmpty(canonicalUrl) ? null : canonicalUrl);
        content.setAllowReplies(allowComments);
        content.updateAuthorAgeRating(authorAgeRating);

        // jw: set the avatarOnDisk to whatever is set on our fields object.
        content.setAvatarImageOnDisk(avatarImageOnDisk);

        // jw: next, let's figure out what Channels we want to publish to.
        Set<Channel> publishToChannels = new HashSet<>();

        if (exists(publishToPrimaryChannel)) {
            publishToChannels.add(publishToPrimaryChannel);
        }

        for (Niche niche : publishToNiches) {
            publishToChannels.add(niche.getChannel());
        }

        // jw: let's create a reference to the ChannelContent lookup map!
        Map<Channel, ChannelContent> channelContentLookup = content.getChannelContentsInited();

        // jw: before we create any new ChannelContent records, let's find any that should be removed.
        Iterator<Map.Entry<Channel, ChannelContent>> channelContentsIterator = channelContentLookup.entrySet().iterator();
        while (channelContentsIterator.hasNext()) {
            ChannelContent channelContent = channelContentsIterator.next().getValue();

            // jw: any channels that this has been blocked from should already be removed from the publishToChannels, so we can safely skip those.
            if (channelContent.getStatus().isBlocked()) {
                continue;
            }

            Channel channel = channelContent.getChannel();
            // jw: if the channel is not in the list of places to publish this, then let's remove it from the map and delete the record.
            if (!publishToChannels.contains(channel)) {
                channelContentsIterator.remove();
                // bl: if the post is being removed from the primary channel, we need to clear any moderation
                if(channel.getType().isPrimaryPublishingChannel() && content.getModerationStatus().isModerated()) {
                    content.approveContent();
                }
                ChannelContent.dao().delete(channelContent);

            // jw: if this content is already associated with the channel then no need to add a new record. Leave the existing one.
            } else {
                publishToChannels.remove(channel);
            }
        }

        boolean isNewToPrimaryChannel = false;
        // jw: finally, let's create any new associations. Can't do this above because we do not want to modify the map
        //     while we are iterating over it.
        for (Channel channel : publishToChannels) {
            if(isEqual(publishToPrimaryChannel, channel)) {
                isNewToPrimaryChannel = true;
            }
            ChannelContent channelContent = new ChannelContent(channel, content);
            ChannelContent.dao().save(channelContent);

            content.getChannelContentsInited().put(channel, channelContent);
        }

        // bl: posts that don't have a title image can not be featured
        if(!content.isHasTitleImage()) {
            ChannelContent channelContent = content.getPrimaryChannelContent();
            if(exists(channelContent)) {
                channelContent.unfeaturePost();
            }
        }

        // bl: if the post is going live for the first time in this channel, then we need to handle moderation.
        // first, it must not be a draft since moderation shouldn't occur for drafts.
        // second, if it's a new post or an edit of a draft (thus publishing the draft), we need to apply moderation
        // finally, if it's an edit of a live post and we're publishing to the primary channel for the first time,
        // then we need to apply moderation
        if(!isDraft() && (isNew || isEditOfDraft() || isNewToPrimaryChannel)) {
            if(exists(publishToPrimaryChannel) && publishToPrimaryChannel.getType().isPublication()) {
                Publication publication = publishToPrimaryChannel.getPublication();
                PublicationSettings settings = publication.getSettings();
                if(settings.isContentModerationEnabled()) {
                    // bl: if content moderation is enabled, then mark the post as needing moderation
                    // bl: by setting the status, we can quickly identify a post's status without needing
                    // to load a ModeratedContent record. ModeratedContent is used for driving the review queues.
                    content.getPrimaryChannelContent().moderatePost();
                    // bl: if the post has never been live before, then let's mark the post as moderated generally
                    // so that it won't be visible anywhere on the platform until the Publication takes action
                    if(isNew || isEditOfDraft()) {
                        content.setModerationStatus(ModerationStatus.MODERATED);
                    }
                    ModeratedContent moderatedContent = new ModeratedContent(content);
                    ModeratedContent.dao().save(moderatedContent);

                    // bl: also send an email to publication editors to let them know there is a new post
                    areaContext().doAreaTask(new SendNewPostNotificationToPublicationEditorsEmailTask(content));
                }
            }
        }
    }

    @Override
    public String getSubjectFieldName() {
        return PostInput.Fields.title;
    }

    @Override
    public String getBodyFieldName() {
        return PostInput.Fields.body;
    }

    @Override
    public int getMinSubjectLength() {
        return ValidPostInputValidator.MIN_TITLE_LENGTH;
    }

    @Override
    public int getMaxSubjectLength() {
        return ValidPostInputValidator.MAX_TITLE_LENGTH;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public Channel getPublishToPrimaryChannel() {
        return publishToPrimaryChannel;
    }

    public void setPublishToPrimaryChannel(Channel publishToPrimaryChannel) {
        this.publishToPrimaryChannel = publishToPrimaryChannel;
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
    }

    public AgeRating getAuthorAgeRating() {
        return authorAgeRating;
    }

    public void setAuthorAgeRating(AgeRating authorAgeRating) {
        this.authorAgeRating = authorAgeRating;
    }

    public ImageOnDisk getAvatarImageOnDisk() {
        return avatarImageOnDisk;
    }

    public void setAvatarImageOnDisk(ImageOnDisk avatarImageOnDisk) {
        this.avatarImageOnDisk = avatarImageOnDisk;
    }

    public Set<Niche> getPublishToNiches() {
        return publishToNiches;
    }

    @Override
    @NotNull
    public ContentType getContentType() {
        return ContentType.NARRATIVE_POST;
    }

    public boolean isPrimaryChannelLocked() {
        return primaryChannelLocked;
    }
}
