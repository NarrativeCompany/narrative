package org.narrative.network.shared.replies.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ValidationHandler;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.content.base.services.NewLiveReplyTask;
import org.narrative.network.core.content.base.services.UpdateFilePointerSet;
import org.narrative.network.core.fileondisk.base.services.CreateUpdateFiles;
import org.narrative.network.core.mentions.MentionsHtmlParser;
import org.narrative.network.core.moderation.Moderatable;
import org.narrative.network.core.moderation.ModeratableStats;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import java.sql.Timestamp;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/21/11
 * Time: 9:18 AM
 *
 * @author brian
 */
public class CreateCompositionReplyTask extends CompositionTaskImpl<OID> {
    private final CompositionConsumer compositionConsumer;
    private final Reply reply;
    private final List<? extends FileData> attachments;

    private String originalBody;
    private boolean isSignificantEdit;

    public CreateCompositionReplyTask(Reply reply) {
        this(null, reply, null);
    }

    public CreateCompositionReplyTask(CompositionConsumer compositionConsumer, Reply reply, List<? extends FileData> attachments) {
        this.compositionConsumer = compositionConsumer;
        this.reply = reply;
        this.attachments = attachments;
    }

    @Override
    protected void validate(ValidationHandler validationHandler) {
        super.validate(validationHandler);

        // bl: be sure to properly validate the guest name if this is a guest post
        // bl: only need to require a guest name if an author was not provided explicitly (e.g. for recipes).
        if (!networkContext().isLoggedInUser() && reply.getUserOid() == null) {
            validationHandler.validateString(reply.getGuestName(), Reply.MIN_GUEST_NAME_LENGTH, Reply.MAX_GUEST_NAME_LENGTH, "replyPublic.guestName", "tags.site.page.inlinePostReplyForm.yourName");
        }

        // bl: validate the length up front to avoid performance issues when checking if it contains visible content
        // due to regex slowness with linkifyEmailAddresses
        if(validationHandler.validateString(reply.getBody(), Reply.MIN_BODY_LENGTH, Reply.MAX_BODY_LENGTH, "replyPublic.body", "postContentReply.body.replyTypeArg0")) {
            // bl: since reply posting happens via a div popup, we can't add any field errors here.
            // only throw exceptions.  input isn't supported (no postReply.jsp anymore).
            // fields should be validated on the client side.
            if (!HTMLParser.doesHtmlFragmentContainVisibleContent(reply.getBody())) {
                // bl: need to make sure that the Reply body actually has a value or else we are going to get Hibernate
                // validation errors on edits due to the fact that Hibernate will try to flush the Reply with an empty
                // body, which is a Hibernate validation error.
                // to fix, let's just evict the Reply from the Hibernate session.  this just means that Hibernate
                // will no longer manage this Reply object, and therefore will not attempt to flush it (thus avoiding
                // any validation errors).  since we know the transaction is going to be rolled back anyway, there is
                // no harm in doing it this way.
                Reply.dao().getGSession().evict(reply);
                validationHandler.addWordletizedFieldError("replyPublic.body", isEmpty(reply.getBody()) ? "error.post.bodyMissing" : "error.post.bodyMissingWithContent");
            }
        }

        if (!isEmptyOrNull(attachments)) {
            for (FileData attachment : attachments) {
                FileData.validateAttachment(validationHandler, attachment);
            }
        }
    }

    @Override
    protected OID doMonitoredTask() {
        // jw: first thing we need to do is escape any and all mentions in the HTML
        // note: this is happening before we do the comparison with the originalBody below, since the original body should
        //       be the original escaped form of the body.
        reply.setBody(MentionsHtmlParser.escapeMentions(reply.getBody()));

        // handle the three different posting cases:
        // 1) guest post - may be a network user posting anonymously
        // 2) area user post
        // 3) registered network user post
        if (reply.isNew() && reply.getUserOid() == null && getNetworkContext().isLoggedInUser()) {
            reply.setGuestName(null);
            reply.setUserOid(getNetworkContext().getUser().getOid());
        }

        boolean charsChanged = false;
        int filesAdded = 0;
        if (compositionConsumer != null) {
            if (!isEmptyOrNull(attachments)) {
                CreateUpdateFiles task = new CreateUpdateFiles(attachments, getNetworkContext().getUser());
                getNetworkContext().doGlobalTask(task);
                filesAdded = task.getFilesAdded();
                if (task.isCharsChanged()) {
                    charsChanged = true;
                }
            }

            UpdateFilePointerSet updateFpsTask = new UpdateFilePointerSet(reply.getComposition(), reply.getFilePointerSet(), attachments);
            FilePointerSet fps = getNetworkContext().doGlobalTask(updateFpsTask);

            reply.setFilePointerSet(exists(fps) ? fps : null);
        }

        // if this is a new Reply, save it.
        if (reply.isNew()) {
            Reply.dao().save(reply);

            // otherwise we still need to flush the session, to ensure new FilePointers are updated with OIDs
        } else {
            PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();
        }

        // scrub the reply, too.
        // bl: needs to happen after all of the OIDs have been generated (specifically, FilePointers).
        reply.scrub();

        final CompositionStats compStats = CompositionStats.dao().getLocked(reply.getComposition().getOid());

        if (reply.isNew()) {
            //update the composition stats
            compStats.addReply();
        } else {
            // if any files have been added or the body has changed, then this is a significant edit
            isSignificantEdit = filesAdded > 0 || charsChanged || !isEqual(reply.getBody(), originalBody);
        }

        // bl: only update edit datetime for significant edits (outside of the grace period)
        if (!reply.isNew() && isSignificantEdit) {
            // bl: don't record edits within the edit grace period.
            //     this is an edit.  security has already been checked
            //     in the validate() method, so this user must be allowed to edit.
            // jw: new business rule, if this is a edit on a question shark reply, and its being edited by a user
            //     other than the original author then we will be sending a notification. and because of that we should
            //     be consistent and set the edit fields on the reply
            reply.setEditDatetime(new Timestamp(System.currentTimeMillis()));
            if (getNetworkContext().isLoggedInUser()) {
                reply.setEditorUserOid(getNetworkContext().getUser().getOid());
            }
        }

        ModerationStatus oldStatus = reply.getModerationStatus();

        // update the moderated item, if necessary.
        // bl: needs to happen after the save of the Reply so we can get the replyOid.
        if (compositionConsumer != null && compositionConsumer.getCompositionType().isSupportsModeration()) {
            //update the content stats
            Moderatable<?, ?, ?> moderatable = cast(compositionConsumer, Moderatable.class);
            ModeratableStats stats = moderatable.getStatsForUpdate();

            ModerationStatus newModerationStatus = reply.getModerationStatus();

            //new reply
            if (reply.isNew()) {
                if (!moderatable.isModerated() && newModerationStatus.isLive()) {
                    getNetworkContext().doGlobalTask(new NewLiveReplyTask(compositionConsumer.getArea(), compositionConsumer, reply, getNetworkContext().getPrimaryRole(), false));
                }

                // edited reply
            } else {
                // went from moderated to not moderated
                assert newModerationStatus.isLive() && !oldStatus.isLive() && newModerationStatus.isLive() : "I don't think this is a valid scenario, but the UpdateReplyModeratedItem could allow it.  See notes in UpdateReplyModeratedItem.setModerationFields()";

                // went from not requiring moderation to requiring moderation?  then update moderated reply counts.
                if (!oldStatus.isRequiresModeration() && newModerationStatus.isRequiresModeration()) {
                    // update the counts
                    stats.addModeratedReply();
                }
            }

            if (reply.isNew()) {
                if (!reply.getModerationStatus().isLive()) {
                    stats.addModeratedReply();
                }
            }
        }

        // jw: now that reply moderation has taken place we need to determine if we need to update the stats
        if (reply.isNew()) {
            // jw: Only update the lastUpdateDatetime if the reply is live (not moderated)
            if (reply.getModerationStatus().isLive()) {
                compStats.setLastUpdateDatetime(reply.getLiveDatetime());
                compStats.setLastReply(reply);
            }
        }

        return reply.getOid();
    }

    public void setOriginalBody(String originalBody) {
        this.originalBody = originalBody;
    }
}
