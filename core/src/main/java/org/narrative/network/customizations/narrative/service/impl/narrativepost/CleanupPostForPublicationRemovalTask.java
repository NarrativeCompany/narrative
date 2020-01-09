package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.services.UpdateFutureContent;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.sql.Timestamp;

/**
 * Date: 2019-09-27
 * Time: 08:49
 *
 * @author jonmark
 */
public class CleanupPostForPublicationRemovalTask extends AreaTaskImpl<Object> {
    private final Content content;
    private final boolean wasModerated;

    public CleanupPostForPublicationRemovalTask(Content content) {
        this.content = content;
        this.wasModerated = content.getPrimaryChannelContent().getStatus().isModerated();
    }

    @Override
    protected Object doMonitoredTask() {
        boolean wasLive = content.isContentLive();

        // bl: always call approveContent to reset the post status to approved
        content.approveContent();

        // bl: if the post wasn't live previously, then the post will go back to the user's drafts
        if(!wasLive) {
            // bl: update the live datetime so it goes back to the top of the user's drafts
            content.updateLiveDatetime(new Timestamp(System.currentTimeMillis()));

            // bl: revert the post back to being a draft
            getAreaContext().doAreaTask(new UpdateFutureContent(content, content.getUser(), true, content.getLiveDatetime()));

            // bl: in addition to the FutureContent, we also need to set the draft flag to true
            content.setDraft(true);
        }

        return null;
    }

    public boolean isWasModerated() {
        return wasModerated;
    }
}
