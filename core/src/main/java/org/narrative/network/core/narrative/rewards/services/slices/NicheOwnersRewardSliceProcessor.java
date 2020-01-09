package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-30
 * Time: 07:17
 *
 * @author brian
 */
public class NicheOwnersRewardSliceProcessor extends RewardSliceProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(NicheOwnersRewardSliceProcessor.class);

    public NicheOwnersRewardSliceProcessor(RewardPeriod period, NrveValue totalNrve) {
        super(period, RewardSlice.NICHE_OWNERS, totalNrve);
    }

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        {
            long incompleteNicheCount = NicheOwnerReward.dao().getIncompleteCountForPeriod(period);
            long totalPoints = NicheContentReward.dao().getTotalPointsForPeriod(period);

            if(logger.isInfoEnabled()) logger.info("Processing " + incompleteNicheCount + " niche owner rewards for " + period.getPeriod() + " with a total of " + totalPoints + " points.");

            // bl: first, we are going to process the individual reward transaction for each niche
            chunkProcess(incompleteNicheCount, true, period, new RewardSliceChunkProcessor(slice, 500) {
                @Override
                protected Integer doMonitoredTask() {
                    List<ObjectPair<NicheOwnerReward,Long>> nicheOwnerRewardAndPoints = NicheOwnerReward.dao().getIncompleteNicheOwnerRewards(period, chunkSize);
                    for (ObjectPair<NicheOwnerReward, Long> pair : nicheOwnerRewardAndPoints) {
                        NicheOwnerReward nicheOwnerReward = pair.getOne();
                        long nichePoints = pair.getTwo();
                        NrveValue rewardAmount = RewardUtils.calculateNrveShare(nrveSlice, nichePoints, totalPoints);
                        WalletTransaction transaction = createTransaction(nicheOwnerReward.getUser().getWallet(), rewardAmount);
                        nicheOwnerReward.setTransaction(transaction);
                    }
                    return nicheOwnerRewardAndPoints.size();
                }
            });
        }

        // bl: now that each niche owner has the reward, we need to make sure we set the NicheContentReward.reward on every record!
        // handle each niche one at a time due to the volume of content and the fact that we have to calculate
        // the total niche points for each niche
        {
            long incompleteNicheCount = NicheContentReward.dao().getIncompleteNicheCountForPeriod(period);

            chunkProcess(incompleteNicheCount, false, null, new RewardSliceChunkProcessor(slice, 1) {
                @Override
                protected Integer doMonitoredTask() {
                    // todo:rewards test to make sure we don't fetch all user data
                    NicheOwnerReward nicheOwnerReward = NicheOwnerReward.dao().getNicheOwnerRewardForContentProcessing(period);
                    if(!exists(nicheOwnerReward)) {
                        return 0;
                    }
                    ObjectPair<Number,Number> contentCountAndTotalNichePoints = NicheContentReward.dao().getIncompleteContentCountAndTotalPointsForNicheReward(nicheOwnerReward.getNicheReward());
                    long incompleteContentCount = contentCountAndTotalNichePoints.getOne().longValue();
                    long totalNichePoints = contentCountAndTotalNichePoints.getTwo().longValue();

                    // todo:rewards make sure we aren't doing one-off queries here to look up the transaction
                    NrveValue nicheOwnerRewardValue = nicheOwnerReward.getTransaction().getNrveAmount();
                    OID nicheRewardOid = nicheOwnerReward.getNicheReward().getOid();

                    chunkProcess(incompleteContentCount, false, null, new RewardSliceChunkProcessor(slice, 500) {
                        @Override
                        protected Integer doMonitoredTask() {
                            NicheReward nicheReward = NicheReward.dao().get(nicheRewardOid);
                            List<NicheContentReward> nicheContentRewards = NicheContentReward.dao().getIncompleteForNicheReward(nicheReward, chunkSize);
                            for (NicheContentReward nicheContentReward : nicheContentRewards) {
                                NrveValue rewardAmount = RewardUtils.calculateNrveShare(nicheOwnerRewardValue, nicheContentReward.getContentReward().getPoints(), totalNichePoints);
                                // bl: we're here just to set the reward on the NicheContentReward
                                nicheContentReward.setReward(rewardAmount);
                            }
                            return nicheContentRewards.size();
                        }
                    });
                    // bl: validate that the sum of NicheContentReward.reward doesn't exceed the sum total of the Niche owner's reward.
                    // could also verify that we are within the margin we expect, just like we do for issuing rewards.
                    NrveValue rewardTotal = NicheContentReward.dao().getRewardTotalForNicheReward(nicheOwnerReward.getNicheReward());
                    if(rewardTotal.compareTo(nicheOwnerRewardValue) > 0) {
                        throw UnexpectedError.getRuntimeException("NicheContentReward distribution exceeded the owner reward. period/" + period.getPeriod() + " nicheReward/" + nicheOwnerReward.getNicheReward().getOid() + " rewardTotal/" + rewardTotal + " nicheOwnerRewardValue/" + nicheOwnerRewardValue);
                    }

                    long contentCount = NicheContentReward.dao().getCountForNicheReward(nicheOwnerReward.getNicheReward());
                    // bl: check that the niche's content reward total is within the margin of error we expect (at most
                    // 1 Neuron lost per piece of content).
                    if(nicheOwnerRewardValue.subtract(rewardTotal).toNeurons() > contentCount) {
                        throw UnexpectedError.getRuntimeException("NicheContentReward distribution total exceeded the expected threshold. period/" + period.getPeriod() + " nicheReward/" + nicheOwnerReward.getNicheReward().getOid() + " nicheOwnerRewardValue/" + nicheOwnerRewardValue + " rewardTotal/" + rewardTotal + " contentCount/" + contentCount);
                    }
                    return 1;
                }
            });
        }

        return null;
    }
}
