package org.narrative.network.customizations.narrative.posts.services;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: brian
 * Date: 3/14/19
 * Time: 8:10 PM
 */
public class SendCommentRemovedFromPublicationPostEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Content content;
    private final Publication publication;
    private final User moderatorUser;

    public SendCommentRemovedFromPublicationPostEmailTask(Content content, Publication publication, User moderatorUser) {
        super(content.getUser());
        this.content = content;
        this.publication = publication;
        this.moderatorUser = moderatorUser;

        assert exists(moderatorUser) : "Should always have a moderatorUser!";
    }

    public Content getContent() {
        return content;
    }

    public Publication getPublication() {
        return publication;
    }

    public User getModeratorUser() {
        return moderatorUser;
    }
}
