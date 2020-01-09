package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Date: 2019-07-01
 * Time: 08:50
 *
 * @author jonmark
 */
public class SendUserNeoWalletChangedEmail extends SendSingleNarrativeEmailTaskBase {
    private final boolean hadPreviousAddress;

    public SendUserNeoWalletChangedEmail(User user, boolean hadPreviousAddress) {
        super(user);

        this.hadPreviousAddress = hadPreviousAddress;
    }

    public boolean isHadPreviousAddress() {
        return hadPreviousAddress;
    }
}
