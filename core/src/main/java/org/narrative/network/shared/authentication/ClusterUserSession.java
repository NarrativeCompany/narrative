package org.narrative.network.shared.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.ClientAgentInformation;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.shared.security.ClusterRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Date: Oct 5, 2010
 * Time: 2:38:04 PM
 *
 * @author brian
 */
public class ClusterUserSession extends UserSession<OID> {

    private final ClusterRole clusterRole;

    public ClusterUserSession(@NotNull ClusterRole clusterRole, @NotNull OID uniqueVisitOid, @NotNull RequestResponseHandler reqResp) {
        super(clusterRole.getFormatPreferences(), uniqueVisitOid, reqResp, uniqueVisitOid);
        this.clusterRole = clusterRole;
    }

    @JsonCreator
    public ClusterUserSession(@JsonProperty("clusterRole") ClusterRole clusterRole,
                              @JsonProperty("creationTime") long creationTime,
                              @JsonProperty("locale") @NotNull Locale locale,
                              @JsonProperty("uniqueVisitOid") @NotNull OID uniqueVisitOid,
                              @JsonProperty("clientAgentInformation") ClientAgentInformation clientAgentInformation,
                              @JsonProperty("sessionKey") OID sessionKey) {
        super(creationTime, locale, uniqueVisitOid, clientAgentInformation, sessionKey);
        this.clusterRole = clusterRole;
    }

    @Override
    public OID getRoleOid() {
        return clusterRole.getOid();
    }

    @Override
    public boolean isLoggedInUser() {
        return true;
    }

    public ClusterRole getClusterRole() {
        return clusterRole;
    }

    @Nullable
    public static ClusterUserSession getClusterUserSession() {
        return (ClusterUserSession) getUserSession();
    }
}
