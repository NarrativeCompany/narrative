package org.narrative.network.core.user;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.NetworkRegistry;

/**
 * Date: 2019-02-19
 * Time: 10:23
 *
 * @author brian
 */
public enum KycQueueAuthRealm implements AuthRealm {
    INSTANCE {
        private final OID NARRATIVE_KYC_QUEUE_AUTH_REALM_OID = new OID(3);

        @Override
        public OID getOid() {
            return NARRATIVE_KYC_QUEUE_AUTH_REALM_OID;
        }

        @Override
        public AuthZone getAuthZone() {
            // bl: no full-fledged AuthZone for AuthRealms.
            return null;
        }

        @Override
        public boolean isSslEnabled() {
            // bl: the Narrative KYC Queue will be SSL. unless it's a local server.
            return !NetworkRegistry.getInstance().isLocalServer();
        }

        @Override
        public String getBaseUrl() {
            // for now, just inherit the current request URL
            return "";
        }

        @Override
        public String getStaticBaseUrl() {
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
