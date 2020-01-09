package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.service.api.model.permissions.StandardRevokeReason;
import lombok.Builder;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-10
 * Time: 16:27
 *
 * @author brian
 */
public class ConductPermissionCheck implements NarrativePermissionCheck {
    private final String permissionRevokedTitleWordletKey;
    private final String permissionRevokedMessageWordletKey;

    @Builder
    public ConductPermissionCheck(String permissionRevokedTitleWordletKey, String permissionRevokedMessageWordletKey) {
        this.permissionRevokedTitleWordletKey = permissionRevokedTitleWordletKey;
        this.permissionRevokedMessageWordletKey = permissionRevokedMessageWordletKey;
    }

    @Override
    public void checkRight(AreaRole areaRole) {
        UserReputation reputation = areaRole.getUser().getReputation();
        if(reputation.isConductNegative()) {
            throw new NarrativePermissionRevokedError(wordlet(permissionRevokedTitleWordletKey), wordlet(permissionRevokedMessageWordletKey), StandardRevokeReason.CONDUCT_NEGATIVE, reputation.getNegativeConductExpirationTimestamp());
        }
    }
}
