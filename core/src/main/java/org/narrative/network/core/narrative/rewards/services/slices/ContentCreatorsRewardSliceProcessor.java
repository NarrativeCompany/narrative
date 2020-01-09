package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.narrative.rewards.ContentReward;
import org.narrative.network.core.narrative.rewards.PublicationReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-30
 * Time: 07:17
 *
 * @author brian
 */
public class ContentCreatorsRewardSliceProcessor extends RewardSliceProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(ContentCreatorsRewardSliceProcessor.class);

    public ContentCreatorsRewardSliceProcessor(RewardPeriod period, NrveValue totalNrve) {
        super(period, RewardSlice.CONTENT_CREATORS, totalNrve);
    }

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        ObjectPair<Number,Number> incompleteCountAndTotalPoints = RoleContentReward.dao().getIncompleteCountAndTotalPointsForPeriod(period);
        long incompleteCount = incompleteCountAndTotalPoints!=null && incompleteCountAndTotalPoints.getOne()!=null ? incompleteCountAndTotalPoints.getOne().longValue() : 0;
        long totalPoints = incompleteCountAndTotalPoints!=null && incompleteCountAndTotalPoints.getTwo()!=null ? incompleteCountAndTotalPoints.getTwo().longValue() : 0;

        if(logger.isInfoEnabled()) logger.info("Processing " + incompleteCount + " ContentReward records for " + period.getPeriod() + " with " + totalPoints + " total points.");

        chunkProcess(incompleteCount, true, period, new RewardSliceChunkProcessor(slice, 500) {
            @Override
            protected Integer doMonitoredTask() {
                List<RoleContentReward> roleContentRewards = RoleContentReward.dao().getIncompleteRoleContentRewards(period, chunkSize);
                for (RoleContentReward roleContentReward : roleContentRewards) {
                    assert roleContentReward.getRole().isWriter() : "Should only get RoleContentReward records here for WRITER roles, not/" + roleContentReward.getRole();
                    ContentReward contentReward = roleContentReward.getContentReward();
                    NrveValue rewardAmount = RewardUtils.calculateNrveShare(nrveSlice, contentReward.getPoints(), totalPoints);
                    PublicationReward publicationReward = contentReward.getPublicationReward();
                    // bl: split up the rewards if the writer doesn't get the full reward amount
                    if(exists(publicationReward) && !publicationReward.getContentRewardWriterShare().isOneHundredPercent()) {
                        NrveValue writerShare = RewardUtils.calculateNrveShare(rewardAmount, publicationReward.getContentRewardWriterShare().getWriterPercentage(), 100);
                        // bl: give the difference to the Publication
                        NrveValue publicationShare = rewardAmount.subtract(writerShare);

                        if(writerShare.equals(NrveValue.ZERO)) {
                            // bl: if the writer share is zero, then delete the record!
                            RoleContentReward.dao().delete(roleContentReward);
                        } else {
                            // bl: if the writerShare is non-zero, record it!
                            recordTransaction(roleContentReward, writerShare);
                        }

                        // bl: now distribute the publication share
                        distributePublicationShare(contentReward, publicationReward, publicationShare);
                    } else {
                        recordTransaction(roleContentReward, rewardAmount);
                    }
                }
                return roleContentRewards.size();
            }

            private void distributePublicationShare(ContentReward contentReward, PublicationReward publicationReward, NrveValue publicationShare) {
                Publication publication = publicationReward.getPublication();
                PublicationContentRewardRecipientType recipient = publicationReward.getContentRewardRecipient();
                List<User> users = new LinkedList<>();
                if(!recipient.isOwner()) {
                    users.addAll(publication.getUsersByRole(recipient.getPublicationRole()));
                }

                // bl: if we couldn't identify any users to distribute the publication's share to, then let's give it all to the owner
                if(users.isEmpty()) {
                    users.add(publication.getOwner());
                }

                // bl: just distribute the NRVE proportionally for all users. works by simply returning 1 NRVE as
                // the share/portion for each user.
                RewardUtils.distributeNrveProportionally(publicationShare, users, (user) -> NrveValue.ONE, (user, nrveValue) -> {
                    // bl: create a new RoleContentReward for each user
                    RoleContentReward roleContentReward = new RoleContentReward(contentReward, recipient.getContentCreatorRewardRole(), user);
                    recordTransaction(roleContentReward, nrveValue);
                    RoleContentReward.dao().save(roleContentReward);
                });
            }

            private void recordTransaction(RoleContentReward roleContentReward, NrveValue rewardAmount) {
                WalletTransaction transaction = createTransaction(roleContentReward.getUser().getWallet(), rewardAmount);
                roleContentReward.setTransaction(transaction);
            }
        });

        return null;
    }
}
