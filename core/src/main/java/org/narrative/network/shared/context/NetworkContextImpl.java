package org.narrative.network.shared.context;

import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.AuthZone;
import org.jetbrains.annotations.NotNull;

/**
 * Date: Dec 22, 2005
 * Time: 5:33:11 PM
 *
 * @author Brian
 */
public class NetworkContextImpl extends NetworkContextImplBase {

    public NetworkContextImpl() {}

    /**
     * get the base URL, which in the case of a NetworkContextImpl,
     * is just a Master URL.
     *
     * @return the Master URL.
     */
    @NotNull
    public String getBaseUrl() {
        return getRequestType().getBaseUrl();
    }

    @Override
    public AuthRealm getAuthRealm() {
        return getRequestType().getSingletonAuthRealm();
    }

    @Override
    public AuthZone getAuthZone() {
        return null;
    }
}
