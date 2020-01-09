package org.narrative.network.core.user.services;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * todo:post-v1.3.2 remove this class and entry in emailMappings.properties when CancelPendingRedemptionsPatch is deleted
 * Date: 2019-08-06
 * Time: 18:10
 *
 * @author brian
 */
public class SendUserRedemptionCanceledEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final WalletTransaction transaction;

    public SendUserRedemptionCanceledEmailTask(User user, WalletTransaction transaction) {
        super(user);
        this.transaction = transaction;
    }

    public WalletTransaction getTransaction() {
        return transaction;
    }
}
