package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-09
 * Time: 13:46
 *
 * @author brian
 */
public class DistributeNicheModeratorRewardsForNicheTask extends RewardSliceRootAreaTask<Object> {
    private final OID nicheRewardOid;
    private final long nichePoints;
    private final long totalPoints;

    private final NrveValue totalNrve;
    private final NrveValue nrveSlice;

    DistributeNicheModeratorRewardsForNicheTask(OID nicheRewardOid, long nichePoints, long totalPoints, NrveValue totalNrve, NrveValue nrveSlice) {
        super(RewardSlice.NICHE_MODERATORS);
        this.nicheRewardOid = nicheRewardOid;
        this.nichePoints = nichePoints;
        this.totalPoints = totalPoints;
        this.totalNrve = totalNrve;
        this.nrveSlice = nrveSlice;
    }

    @Override
    protected Object doMonitoredTask() {
        NicheReward nicheReward = NicheReward.dao().get(nicheRewardOid);

        // verify that the percentage we're using for moderators equals the niche owner's share percentage.
        // the nichePoints and totalPoints _should_ match the values used by the Niche, so just
        // calculate the expected owner payout according to the values and make sure it matches
        // the actual owner payout. if it does, then we know we have the correct values for moderator reward calculation!
        {
            NicheOwnerReward nicheOwnerReward = NicheOwnerReward.dao().getForNicheReward(nicheReward);
            assert exists(nicheOwnerReward.getTransaction()) : "Should always process all NicheOwnerReward records before processing moderators! nicheOwnerReward/" + nicheOwnerReward.getOid();
            NrveValue totalNicheOwnerRewards = RewardSlice.NICHE_OWNERS.getSliceNrve(totalNrve);
            NrveValue expectedNicheOwnerReward = RewardUtils.calculateNrveShare(totalNicheOwnerRewards, nichePoints, totalPoints);
            if(!expectedNicheOwnerReward.equals(nicheOwnerReward.getTransaction().getNrveAmount())) {
                throw UnexpectedError.getRuntimeException("The Niche moderator share doesn't match the Niche owner share! period/" + period.getPeriod() + " expectedNicheOwnerReward/" + expectedNicheOwnerReward + " actualNicheOwnerReward/" + nicheOwnerReward.getTransaction().getNrveAmount() + " nichePoints/" + nichePoints + " totalPoints/" + totalPoints + " totalNrve/" + totalNrve + " totalNicheOwnerRewards/" + totalNicheOwnerRewards);
            }
        }

        List<NicheModeratorReward> nicheModeratorRewards = nicheReward.getNicheModeratorRewards();
        // bl: now calculate the share that each moderator gets of the total niche reward amount.
        // distributed evenly across each moderator.

        // bl: first, calculate the niche's total share of NRVE for moderators
        NrveValue totalNicheModeratorRewardAmount = RewardUtils.calculateNrveShare(nrveSlice, nichePoints, totalPoints);

        NrveValue moderatorReward = RewardUtils.calculateNrveShare(totalNicheModeratorRewardAmount, 1, nicheModeratorRewards.size());
        for (NicheModeratorReward nicheModeratorReward : nicheModeratorRewards) {
            WalletTransaction transaction = createTransaction(nicheModeratorReward.getUser().getWallet(), moderatorReward);
            nicheModeratorReward.setTransaction(transaction);
        }
        return null;
    }
}
