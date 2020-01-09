package org.narrative.network.core.content.base.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.services.NewLivePostTask;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Date: May 12, 2006
 * Time: 9:16:45 AM
 *
 * @author Brian
 */
public class NewLiveReplyTask extends GlobalTaskImpl<Object> {

    private final Area area;
    private final CompositionConsumer<?, ?, ?> compositionConsumer;
    private final Reply reply;
    private final PrimaryRole authorRole;
    private final boolean wasReplyPreviouslyLive;

    public NewLiveReplyTask(Area area, CompositionConsumer<?, ?, ?> compositionConsumer, Reply reply, PrimaryRole authorRole, boolean wasReplyPreviouslyLive) {
        this.area = area;
        this.compositionConsumer = compositionConsumer;
        this.reply = reply;
        this.authorRole = authorRole;
        this.wasReplyPreviouslyLive = wasReplyPreviouslyLive;
    }

    protected Object doMonitoredTask() {
        assert compositionConsumer.isLive() : "Shouldn't use the NewLiveReplyTask unless the Content is live!";
        assert reply.getModerationStatus().isLive() : "Shouldn't use the NewLiveReplyTask unless the Comment is live!";

        // jw: increment the post count for this reply if the reply was not previously live.
        if (!wasReplyPreviouslyLive) {
            getNetworkContext().doAreaTask(area, new NewLivePostTask(compositionConsumer, reply));
        }

        return null;
    }
}
