package org.narrative.network.core.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.niche.services.RevokeNicheOwnershipTask;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 5/10/18
 * Time: 10:17 AM
 */
public class CleanupNichesForDeletedOrDeactivatedUserTask extends AreaTaskImpl<Object> {
    private final User user;

    public CleanupNichesForDeletedOrDeactivatedUserTask(User user) {
        this.user = user;
    }

    @Override
    protected Object doMonitoredTask() {
        AreaUserRlm areaUserRlm = AreaUser.getAreaUserRlm(user.getLoneAreaUser());

        // jw: since the RevokeNicheOwnershipTask will be removing the association we need to clone the values collection
        for (NicheUserAssociation association : new ArrayList<>(areaUserRlm.getNicheUserAssociations().values())) {
            // jw: if he is not the owner, short out and keep the item in the map.
            if (!association.getType().isOwner()) {
                return false;
            }

            // jw: yay, we can clear this user as the owner
            getAreaContext().doAreaTask(new RevokeNicheOwnershipTask(association.getNiche()));
        }

        // jw: now that all owned niches have been cleared, let's ensure that the user is not a member of the niche owners circle.
        assert !user.getLoneAreaUser().getAreaCircleUsersInited().containsKey(NarrativeCircleType.NICHE_OWNERS.getCircle(user.getAuthZone())) : "The RevokeNicheOwnershipTask should have removed the user from the niche owners task when invoked for the last niche that the user owned.";

        return null;
    }
}
