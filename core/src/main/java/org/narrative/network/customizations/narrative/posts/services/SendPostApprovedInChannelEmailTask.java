package org.narrative.network.customizations.narrative.posts.services;

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
public class SendPostApprovedInChannelEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Content content;
    private final Channel channel;
    private final User moderatorUser;

    public SendPostApprovedInChannelEmailTask(Content content, Channel channel, User moderatorUser) {
        super(content.getUser());
        this.content = content;
        this.channel = channel;
        this.moderatorUser = moderatorUser;

        assert exists(moderatorUser) : "Should always have a moderatorUser!";
        assert channel.getType().isPublication() : "Only support moderation in Publications currently!";
    }

    public Content getContent() {
        return content;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getModeratorUser() {
        return moderatorUser;
    }
}
