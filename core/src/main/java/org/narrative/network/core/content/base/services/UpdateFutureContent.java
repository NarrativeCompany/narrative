package org.narrative.network.core.content.base.services;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.FutureContent;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: May 12, 2006
 * Time: 3:27:50 PM
 *
 * @author Brian
 */
public class UpdateFutureContent extends AreaTaskImpl<FutureContent> {
    private final Content content;
    private final User user;
    private final boolean isDraft;
    private final Timestamp saveDatetime;

    public UpdateFutureContent(Content content, User user, boolean draft, Timestamp saveDatetime) {
        this.content = content;
        this.user = user;
        isDraft = draft;
        this.saveDatetime = saveDatetime;
    }

    protected FutureContent doMonitoredTask() {
        FutureContent fc = content.getFutureContent();

        // if the content isn't live (i.e. draft, future pub, or moderated content), then create FutureContent.
        if (isDraft) {
            if (!exists(fc)) {
                fc = new FutureContent(content);
            }

            fc.setSaveDatetime(saveDatetime);

            fc.setUser(user);

            fc.setDraft(isDraft);

            return fc;
        }

        // no future content necessary since this content is live.  delete it if necessary.
        content.setFutureContent(null);
        if (exists(fc)) {
            FutureContent.dao().delete(fc);

        }
        return null;
    }
}
