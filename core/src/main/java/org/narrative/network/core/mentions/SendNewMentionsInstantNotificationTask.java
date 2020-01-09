package org.narrative.network.core.mentions;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.PostBase;
import org.narrative.network.core.composition.base.PostMentionsConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.MobilePushNotificationTask;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/27/16
 * Time: 10:33 AM
 */
public class SendNewMentionsInstantNotificationTask extends CompositionTaskImpl<Object> implements MobilePushNotificationTask {
    private static final NetworkLogger logger = new NetworkLogger(SendNewMentionsInstantNotificationTask.class);

    private final CompositionConsumer consumer;
    private final OID replyOid;
    private Reply reply;
    private Composition composition;

    protected SendNewMentionsInstantNotificationTask(CompositionConsumer consumer, OID replyOid) {
        assert exists(consumer) : "Should never run this task without providing a consumer!";

        this.consumer = consumer;
        this.replyOid = replyOid;
    }

    private User userToNotify;

    @Override
    protected Object doMonitoredTask() {
        PostMentionsConsumer mentionsConsumer;

        boolean forReply = replyOid != null;
        // jw: load the reply if we have one.
        if (forReply) {
            reply = Reply.dao().get(replyOid);
            mentionsConsumer = reply;

            // jw: if we failed to get a reply despite having a replyOid we need to just short out.
            if (!exists(reply)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Shorting out of mention notification due to missing mentionsConsumer: for consumer/" + consumer.getOid() + "and replyOid/" + (replyOid != null ? replyOid : ""));
                }

                return null;
            }

        } else {
            // jw: we only need the composition when we do not have a reply, so that we can show the post body for the
            //     message that the member was mentioned in.
            composition = Composition.dao().get(consumer.getCompositionOid());
            mentionsConsumer = composition;
        }

        // jw: short out if the reply no longer exists.
        if (!exists(mentionsConsumer)) {
            throw UnexpectedError.getRuntimeException("We should always have a MentionsConsumer by this point!");
        }

        Set<String> emailList = new HashSet<>();
        // jw: due to erasures on default methods we need to assign the result of this call into a variable in order
        //     to be able to use the list in the for loop.
        List<User> membersToNotify = mentionsConsumer.getMentionedMembersToNotify();
        if (!isEmptyOrNull(membersToNotify)) {
            for (User user : membersToNotify) {
                if (isEqual(user, mentionsConsumer.getUser())) {
                    continue;
                }

                AreaRole areaRole = user.getAreaRoleForArea(consumer.getArea());

                // jw: if they cannot view the consumer, then they cant view the mention.
                if (!consumer.hasViewRight(areaRole)) {
                    continue;
                }

                userToNotify = user;
                user.sendInstantNotification(this);

                if (logger.isDebugEnabled()) {
                    emailList.add(user.getEmailAddress());
                }
            }

            mentionsConsumer.addNotifiedMentionedMembers(membersToNotify);
        }

        if (logger.isDebugEnabled()) {
            String emails = IPStringUtil.getCommaSeparatedList(emailList).toString();
            logger.debug("Sending mention notification email(s): for consumer/" + consumer.getOid() + " emails/" + emails);
        }

        return null;
    }

    public CompositionConsumer getConsumer() {
        return consumer;
    }

    public Reply getReply() {
        return reply;
    }

    public PostBase getPost() {
        if (exists(reply)) {
            return reply;
        }

        return composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String getNotificationMessage() {
        boolean useConsumerType = !getConsumer().getCompositionConsumerType().isReferendum();
        StringBuilder typeName = new StringBuilder(useConsumerType ? getConsumer().getTypeNameForDisplay() : "");
        if (exists(getReply())) {
            if (useConsumerType) {
                typeName.append(" ");
            }
            typeName.append(wordlet("content.comment"));
        }
        return wordlet("jsp.site.email.emailNewMention.subject", typeName.toString());
    }

    @Override
    public String getTargetUrl() {
        if (exists(getReply())) {
            return getConsumer().getDisplayReplyUrl(getReply().getOid());
        }
        return getConsumer().getDisplayUrl();
    }

    // jw: for consistency sake, lets use the same URL for the action in the email as we use for the target of the push notification.
    public String getActionUrl() {
        return getTargetUrl();
    }

}
