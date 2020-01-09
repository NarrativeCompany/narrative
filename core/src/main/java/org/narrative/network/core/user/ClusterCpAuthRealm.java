package org.narrative.network.core.user;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.NetworkRegistry;

/**
 * Date: 2/23/15
 * Time: 4:40 PM
 *
 * @author brian
 */
public enum ClusterCpAuthRealm implements AuthRealm {
    INSTANCE {
        private final OID CLUSTER_CP_AUTH_REALM_OID = new OID(2);

        @Override
        public OID getOid() {
            return CLUSTER_CP_AUTH_REALM_OID;
        }

        @Override
        public AuthZone getAuthZone() {
            // bl: no full-fledged AuthZone for AuthRealms.
            return null;
        }

        @Override
        public boolean isSslEnabled() {
            // bl: no cluster CPs are SSL now. they're protected behind firewalls, not exposed to the public.
            return false;
        }

        @Override
        public String getBaseUrl() {
            // bl: use relative URLs always for the cluster CP now
            return NetworkRegistry.getInstance().getClusterCpRelativePath();
        }

        @Override
        public String getStaticBaseUrl() {
            // bl: use relative URLs always for cluster CP now
            return NetworkRegistry.getInstance().getStaticPath();
        }

        @Override
        public String getBaseUrlForCurrentScheme() {
            // bl: don't ever change the cluster URL based on scheme. should always be HTTP (dev/qa) or HTTPS (production)
            return getBaseUrl();
        }

        @Override
        public String getStaticBaseUrlForCurrentScheme() {
            // bl: don't ever change the cluster URL based on scheme. should always be HTTP (dev/qa) or HTTPS (production)
            return getStaticBaseUrl();
        }

        @Override
        public DefaultLocale getDefaultLocale() {
            return DefaultLocale.getDefault();
        }

    };
}
