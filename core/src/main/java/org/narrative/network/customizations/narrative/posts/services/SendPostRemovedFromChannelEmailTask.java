package org.narrative.network.customizations.narrative.posts.services;

import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: brian
 * Date: 3/14/19
 * Time: 8:10 PM
 */
public class SendPostRemovedFromChannelEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Content content;
    private final Channel channel;
    private final boolean forChannelDeletion;
    private final User moderatorUser;
    private final boolean forModeration;
    private final String message;

    public SendPostRemovedFromChannelEmailTask(Content content, Channel channel, boolean forChannelDeletion, User moderatorUser, boolean forModeration, String message) {
        super(content.getUser());
        this.content = content;
        this.channel = channel;
        this.forChannelDeletion = forChannelDeletion;
        this.moderatorUser = moderatorUser;
        this.forModeration = forModeration;
        this.message = MessageTextMassager.getMassagedTextForBasicTextarea(message, true);

        assert !forModeration || exists(moderatorUser) : "Should always have a moderatorUser for posts removed for moderation! Will need to update JSP to support this scenario if it's needed.";
        assert !forModeration || channel.getType().isPublication() : "Only support moderation in Publications currently! Will need to update JSP to support this scenario if it's needed.";
    }

    public Content getContent() {
        return content;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isForChannelDeletion() {
        return forChannelDeletion;
    }

    public User getModeratorUser() {
        return moderatorUser;
    }

    public boolean isForModeration() {
        return forModeration;
    }

    public String getMessage() {
        return message;
    }
}
