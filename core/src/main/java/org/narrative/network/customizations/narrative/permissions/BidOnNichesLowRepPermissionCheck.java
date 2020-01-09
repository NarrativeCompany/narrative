package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.permissions.BidOnNichesRevokeReason;
import org.narrative.network.customizations.narrative.service.api.model.permissions.StandardRevokeReason;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-04-17
 * Time: 14:13
 *
 * @author jonmark
 */
public class BidOnNichesLowRepPermissionCheck implements NarrativePermissionCheck {
    @Override
    public void checkRight(AreaRole areaRole) {
        User user = areaRole.getUser();

        // jw: if the user is not low rep then they are good to go.
        if (!user.getReputation().getLevel().isLow()) {
            return;
        }

        // jw: If the user already owns a Niche then they are good to go.
        if (!AreaUser.getAreaUserRlm(user.getLoneAreaUser()).getOwnedNiches().isEmpty()) {
            return;
        }

        // jw: if the user can make fiat payments then let's give them that reason since it will allow them to bypass
        //     the low rep security check.
        if (user.isCanMakeFiatPayments()) {
            throw new NarrativePermissionRevokedError(
                    wordlet("managedNarrativeCircleType.accessError.title.bidOnNiches"),
                    wordlet("nicheAuction.securityDepositRequired"),
                    BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED,
                    null
            );
        }

        // jw: guess we need to give the low rep reason.
        throw new NarrativePermissionRevokedError(wordlet("managedNarrativeCircleType.accessError.title.bidOnNiches"), wordlet("managedNarrativeCircleType.accessError.lowReputation.bidOnNiches"), StandardRevokeReason.LOW_REPUTATION, null);
    }
}
