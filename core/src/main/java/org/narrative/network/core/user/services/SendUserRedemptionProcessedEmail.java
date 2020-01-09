package org.narrative.network.core.user.services;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Date: 2019-07-02
 * Time: 08:25
 *
 * @author brian
 */
public class SendUserRedemptionProcessedEmail extends SendSingleNarrativeEmailTaskBase {
    private final WalletTransaction transaction;

    public SendUserRedemptionProcessedEmail(User user, WalletTransaction transaction) {
        super(user);
        this.transaction = transaction;
    }

    public WalletTransaction getTransaction() {
        return transaction;
    }
}
