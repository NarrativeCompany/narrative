package org.narrative.network.customizations.narrative.service.impl.comment;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.mentions.MentionsAdapter;
import org.narrative.network.core.user.UserActivityRateLimit;
import org.narrative.network.core.user.services.activityrate.CheckUserActivityRateLimitTask;
import org.narrative.network.customizations.narrative.comments.services.CreateCommentTask;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-13
 * Time: 13:07
 *
 * @author jonmark
 */
public class CreateEditCommentTask extends AreaTaskImpl<Reply> {
    private final CompositionConsumer consumer;
    private final String body;
    private final Reply replyForEdit;

    public CreateEditCommentTask(CompositionConsumer consumer, String body, Reply replyForEdit) {
        this.consumer = consumer;
        this.body = body;
        this.replyForEdit = replyForEdit;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        getAreaContext().getAreaRole().checkNarrativeRight(NarrativePermissionType.POST_COMMENTS);

        if (exists(replyForEdit)) {
            replyForEdit.checkEditableByCurrentUser();
        } else {
            consumer.checkRightToReply(getAreaContext().getAreaRole());

            // jw: ensure that the user has not hit their limit for posting replies.
            getAreaContext().doAreaTask(new CheckUserActivityRateLimitTask(getAreaContext().getUser(), UserActivityRateLimit.COMMENT));
        }
    }

    @Override
    protected Reply doMonitoredTask() {
        Reply reply;
        if(exists(replyForEdit)) {
            reply = replyForEdit;
        } else {
            reply = new Reply(consumer.getCompositionCache().getComposition(), getNetworkContext().getUser());
        }
        String originalBody = reply.getBody();
        String body = MessageTextMassager.getMassagedTextForBasicTextarea(this.body, true);
        body = MentionsAdapter.convertMentionsToHtml(body);
        reply.setBody(body);
        OID replyOid = getAreaContext().doAreaTask(new CreateCommentTask(consumer, reply, originalBody));
        return Reply.dao().get(replyOid);
    }
}
