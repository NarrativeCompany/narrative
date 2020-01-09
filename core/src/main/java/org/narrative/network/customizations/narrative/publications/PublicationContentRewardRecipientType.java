package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.*;
import org.narrative.network.core.narrative.rewards.ContentCreatorRewardRole;

/**
 * Date: 2019-08-26
 * Time: 14:52
 *
 * @author jonmark
 */
public enum PublicationContentRewardRecipientType implements IntegerEnum {
    OWNER(0, ContentCreatorRewardRole.PUBLICATION_OWNER, null),
    ADMINS(1, ContentCreatorRewardRole.PUBLICATION_ADMIN, PublicationRole.ADMIN),
    EDITORS(2, ContentCreatorRewardRole.PUBLICATION_EDITOR, PublicationRole.EDITOR),
    ;

    private final int id;
    private final ContentCreatorRewardRole contentCreatorRewardRole;
    private final PublicationRole publicationRole;

    PublicationContentRewardRecipientType(int id, ContentCreatorRewardRole contentCreatorRewardRole, PublicationRole publicationRole) {
        this.id = id;
        this.contentCreatorRewardRole = contentCreatorRewardRole;
        this.publicationRole = publicationRole;
    }

    @Override
    public int getId() {
        return id;
    }

    public ContentCreatorRewardRole getContentCreatorRewardRole() {
        return contentCreatorRewardRole;
    }

    public PublicationRole getPublicationRole() {
        return publicationRole;
    }

    public boolean isOwner() {
        return this==OWNER;
    }
}