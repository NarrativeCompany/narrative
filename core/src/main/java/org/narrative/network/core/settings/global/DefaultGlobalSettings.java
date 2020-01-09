package org.narrative.network.core.settings.global;

import org.narrative.network.core.propertyset.base.services.PropertySetTypeDefaultValueProvider;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;

/**
 * Date: Dec 7, 2005
 * Time: 2:03:18 PM
 *
 * @author Brian
 */
public class DefaultGlobalSettings implements PropertySetTypeDefaultValueProvider<GlobalSettings> {
    public static final GlobalSettings DEFAULT_GLOBAL_SETTINGS;

    static {
        GlobalSettings s = PropertySetTypeUtil.getEmptyPropertyWrapper(GlobalSettings.class);

        s.setSolrIndexVersion(0);

        DEFAULT_GLOBAL_SETTINGS = s;
    }

    public synchronized GlobalSettings getDefaultPropertySet() {
        return DEFAULT_GLOBAL_SETTINGS;
    }
}
