package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.customizations.narrative.publications.Publication;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-09-20
 * Time: 15:34
 *
 * @author brian
 */
public class SendPublicationWriterAccountDeletedToPublicationEditorsEmailTask extends SendPublicationEditorsEmailTaskBase {
    private final String userDisplayName;
    private final int deletedPostCount;

    public SendPublicationWriterAccountDeletedToPublicationEditorsEmailTask(Publication publication, String userDisplayName, int deletedPostCount) {
        super(publication);
        assert !isEmpty(userDisplayName) : "Should always specify the deleted writer's name!";
        assert deletedPostCount>0 : "Should only use this task for users who have at least 1 post deleted!";
        this.userDisplayName = userDisplayName;
        this.deletedPostCount = deletedPostCount;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public int getDeletedPostCount() {
        return deletedPostCount;
    }
}
