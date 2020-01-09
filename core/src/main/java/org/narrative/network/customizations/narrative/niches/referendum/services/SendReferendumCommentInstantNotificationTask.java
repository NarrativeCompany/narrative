package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.user.User;
import org.narrative.network.core.watchlist.services.SendCompositionConsumerCommentInstantNotificationTaskBase;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;

import java.util.HashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/2/18
 * Time: 2:53 PM
 */
public class SendReferendumCommentInstantNotificationTask extends SendCompositionConsumerCommentInstantNotificationTaskBase<Referendum> {
    private final Referendum referendum;

    public SendReferendumCommentInstantNotificationTask(Referendum referendum, OID replyOid) {
        super(referendum, replyOid);

        this.referendum = referendum;
    }

    @Override
    protected Set<OID> getGloballyWatchingUsers(Area area, User author, boolean isForReply) {
        assert isForReply : "For referendums, this should only ever be for replies!";
        assert referendum.getType().isTribunalReferendum() : "Should only be sending comment notifications for tribunal referendums!";

        Set<OID> results = new HashSet<>();

        // jw: let's add anyone watching this referendum for comments
        results.addAll(FollowedChannel.dao().getUserOidsInCircleFollowing(
                referendum.getChannel(),
                NarrativeCircleType.TRIBUNAL.getCircle(getNetworkContext().getAuthZone())
        ));

        if (exists(author)) {
            results.remove(author.getOid());
        }

        return results;
    }

    public Referendum getReferendum() {
        return referendum;
    }

}
