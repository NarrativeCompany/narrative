package org.narrative.network.core.settings.area.community.advanced;

import org.narrative.common.util.TimeZoneWrapper;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeDefaultValueProvider;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.system.NetworkRegistry;

/**
 * Date: Oct 4, 2010
 * Time: 4:16:29 PM
 *
 * @author brian
 */
public class DefaultSandboxedCommunitySettings implements PropertySetTypeDefaultValueProvider<SandboxedCommunitySettings> {
    @Override
    public SandboxedCommunitySettings getDefaultPropertySet() {
        SandboxedCommunitySettings s = PropertySetTypeUtil.getEmptyPropertyWrapper(SandboxedCommunitySettings.class);
        applyDefaultSandboxedCommunitySettings(s);

        return s;
    }

    public static void applyDefaultSandboxedCommunitySettings(SandboxedCommunitySettings s) {
        s.setDefaultGuestTimeZone(TimeZoneWrapper.DEFAULT_TIME_ZONE);

        s.setDefaultLocale(DefaultLocale.ENGLISH);

        s.setNrveScriptHash(NetworkRegistry.getInstance().isProductionServer() ? "a721d5893480260bd28ca1f395f2c465d0b5b1c2" : "583d08308c7cb5a9ea567a2aae5acdd35c77d19b");
        if(NetworkRegistry.getInstance().isProductionServer()) {
            s.setExtraNarrativeCompanyNeoWalletAddress("AamcXvdhmc8QD25e3tBGJeh2T9wsqjEhiq");
        }
        s.setFiatPaymentsEnabled(true);
    }
}
