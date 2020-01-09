package org.narrative.network.core.user;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;

/**
 * Date: 2/23/15
 * Time: 4:35 PM
 *
 * @author brian
 */
public interface AuthRealm {
    public OID getOid();

    public AuthZone getAuthZone();

    public boolean isSslEnabled();

    public String getBaseUrl();

    public String getStaticBaseUrl();

    public String getBaseUrlForCurrentScheme();

    public String getStaticBaseUrlForCurrentScheme();

    public DefaultLocale getDefaultLocale();

}
