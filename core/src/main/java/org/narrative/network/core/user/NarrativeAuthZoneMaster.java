package org.narrative.network.core.user;

import org.narrative.network.core.system.NetworkRegistry;

/**
 * Date: Nov 19, 2010
 * Time: 2:55:43 PM
 *
 * @author brian
 */
public enum NarrativeAuthZoneMaster implements AuthZoneMaster {
    INSTANCE;

    public static final String NARRATIVE_NAME = "Narrative";

    @Override
    public String getBaseUrl() {
        return getSecureBaseUrl();
    }

    @Override
    public String getSecureBaseUrl() {
        return NetworkRegistry.getInstance().getClusterCpUrl();
    }

    @Override
    public String getReplyToEmailAlias() {
        return NARRATIVE_NAME;
    }

    @Override
    public String getReplyToEmailAddress() {
        return "alerts@narrative.org";
    }

}
