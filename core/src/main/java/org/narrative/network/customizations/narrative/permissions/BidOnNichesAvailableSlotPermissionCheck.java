package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.service.api.model.permissions.BidOnNichesRevokeReason;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-10
 * Time: 16:24
 *
 * @author brian
 */
public class BidOnNichesAvailableSlotPermissionCheck implements NarrativePermissionCheck {
    @Override
    public void checkRight(AreaRole areaRole) {
        // bl: if the user has the ability generally to bid on niches, then we just need to make sure that
        // the user has an available niche slot! note that it's possible one of the niche slots is reserved
        // by a niche currently up for auction (where the user is the leading bidder). in that case,
        // this method will actually throw an exception since we don't have the niche in context at this point.
        // thus, any calling code will need to handle that gracefully.
        AreaUserRlm areaUserRlm = areaRole.getAreaUserRlm();

        // bl: if the user is out of slots, then they don't have permission generally
        if (areaUserRlm.getAvailableNicheAssociationSlots().isEmpty()) {
            throw new NarrativePermissionRevokedError(wordlet("managedNarrativeCircleType.accessError.title.bidOnNiches"), wordlet("nicheAuction.youHaveNoFreeSlotsForBidding"), BidOnNichesRevokeReason.NICHE_SLOTS_FULL, null);
        }
    }
}
