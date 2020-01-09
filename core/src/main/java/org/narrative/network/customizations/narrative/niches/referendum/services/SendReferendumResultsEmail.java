package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;

import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/3/18
 * Time: 10:01 AM
 */
public class SendReferendumResultsEmail extends SendBulkNarrativeEmailTaskBase {
    private Referendum referendum;

    public SendReferendumResultsEmail(Referendum referendum) {
        this.referendum = referendum;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        return FollowedChannel.dao().getUserOidsFollowing(referendum.getChannel());
    }

    @Override
    protected void setupForChunk(List<User> users) {
        referendum = Referendum.dao().get(referendum.getOid());
    }

    public Referendum getReferendum() {
        return referendum;
    }

    public static SendReferendumResultsEmail getInstanceForPreview(ReferendumType type, boolean wasPassed, boolean unanimous) {
        return new SendReferendumResultsEmail(Referendum.getInstanceForPreviewEmail(type, wasPassed, unanimous)) {
            protected List<OID> getUserOidsToNotify() {
                throw UnexpectedError.getRuntimeException("Should never run this task!");
            }

            public User getUser() {
                return networkContext().getUser();
            }
        };
    }
}
