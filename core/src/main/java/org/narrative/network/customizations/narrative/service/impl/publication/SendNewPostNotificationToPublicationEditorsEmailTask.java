package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.content.base.Content;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendNewPostNotificationToPublicationEditorsEmailTask extends SendPublicationEditorsEmailTaskBase {
    private final Content content;

    public SendNewPostNotificationToPublicationEditorsEmailTask(Content content) {
        super(content.getSubmittedToPublication());
        assert exists(content.getSubmittedToPublication()) : "Should only use this task for posts made in a publication!";
        assert content.getPrimaryChannelContent().getStatus().isModerated() : "Should only use this task for posts that are pending review!";
        this.content = content;
    }

    public Content getContent() {
        return content;
    }
}
