package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.network.core.narrative.rewards.ContentReward;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.PublicationReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.shared.util.NetworkLogger;

/**
 * Date: 2019-05-27
 * Time: 16:52
 *
 * @author brian
 */
public class PopulateContentActivityStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(PopulateContentActivityStepProcessor.class);

    public PopulateContentActivityStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.POPULATE_CONTENT_ACTIVITY);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: take a snapshot up front of the niche-content associations so that we can use it to consistently
        // drive the rest of the insert statements. this ensures consistent processing even if niche content
        // associations change during the processing of this task. note that this temporary table will only
        // include content that meets our reward requirements:
        // * exclude low quality content
        // * exclude content not posted to a niche

        // bl: first, create the temp tables
        timeExecution("Creating tmp_NicheContent and tmp_PublicationContent tables", () -> ContentReward.dao().runForAutoCommit(() -> {
            ContentReward.dao().createTemporaryNicheContentTable();
            ContentReward.dao().createTemporaryPublicationContentTable();
        }));

        // bl: snapshot Publication settings into PublicationReward
        timeExecution("Inserting PublicationReward records for period/" + period.getPeriod(), () -> PublicationReward.dao().insertPublicationRewardsForPeriod(period));

        // bl: inserting into ContentReward is a single bulk insert, as crazy as that is!
        timeExecution("Inserting ContentReward records for period/" + period.getPeriod(), () -> ContentReward.dao().insertContentRewardsForPeriod(period));

        // bl: then insert the base WRITER RoleContentReward records
        timeExecution("Inserting RoleContentReward records for period/" + period.getPeriod(), () -> RoleContentReward.dao().insertRoleContentRewardsForPeriod(period));

        // bl: in the same transaction, let's go ahead and create the NicheReward records so that we
        // capture the snapshot of niches the content was posted to
        timeExecution("Inserting NicheReward records for period/" + period.getPeriod(), () -> NicheReward.dao().insertNicheRewardsForPeriod(period));

        // bl: create the NicheContentReward records.
        timeExecution("Inserting NicheContentReward records for period/" + period.getPeriod(), () -> NicheContentReward.dao().insertNicheContentRewardsForPeriod(period));

        // bl: create the NicheOwnerReward records. excludes deleted owner users.
        timeExecution("Inserting NicheOwnerReward records for period/" + period.getPeriod(), () -> NicheOwnerReward.dao().insertNicheOwnerRewardsForPeriod(period));

        // bl: create the NicheModeratorReward records. excludes deleted moderator users.
        // bl: for now, owners are the only moderators, so this insert just inserts a moderator record for the owner of each niche
        timeExecution("Inserting NicheModeratorReward records for period/" + period.getPeriod(), () -> NicheModeratorReward.dao().insertNicheModeratorRewardsForPeriod(period));

        // bl: finally, drop the temp tables
        timeExecution("Dropping tmp_NicheContent and tmp_PublicationContent tables", () -> {
            ContentReward.dao().dropTemporaryNicheContentTable();
            ContentReward.dao().dropTemporaryPublicationContentTable();
        });
        return null;
    }
}
