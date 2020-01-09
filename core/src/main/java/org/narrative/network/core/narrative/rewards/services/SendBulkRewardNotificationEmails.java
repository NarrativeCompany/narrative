package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.services.SendBulkNarrativeEmailTaskBase;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date: 2019-06-03
 * Time: 08:29
 *
 * @author brian
 */
public class SendBulkRewardNotificationEmails extends SendBulkNarrativeEmailTaskBase {
    private static final NetworkLogger logger = new NetworkLogger(SendBulkRewardNotificationEmails.class);

    private final RewardPeriod rewardPeriod;
    private final Map<OID, NrveValue> userOidToReward;

    public SendBulkRewardNotificationEmails(RewardPeriod rewardPeriod, Map<OID, NrveValue> userOidToReward) {
        this.rewardPeriod = rewardPeriod;
        this.userOidToReward = userOidToReward;
    }

    // jw: this will cause logging to be put out for every chunk of users we send an email to.
    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected List<OID> getUserOidsToNotify() {
        return new ArrayList<>(userOidToReward.keySet());
    }

    @Override
    protected void setupForChunk(List<User> users) {
        // bl: nothing special to set up here
    }

    @Override
    protected boolean isRunSynchronously() {
        // bl: we want the emails sent synchronously, not at end of partition group
        return true;
    }

    public RewardPeriod getRewardPeriod() {
        return rewardPeriod;
    }

    public NrveValue getReward() {
        // bl: we can just pull the reward out of the map based on the current user the email is going to!
        return userOidToReward.get(getUser().getOid());
    }
}
