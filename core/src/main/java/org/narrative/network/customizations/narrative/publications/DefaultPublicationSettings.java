package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.web.HorizontalAlignment;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeDefaultValueProvider;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;

/**
 * Date: 2019-07-31
 * Time: 08:54
 *
 * @author jonmark
 */
public class DefaultPublicationSettings implements PropertySetTypeDefaultValueProvider<PublicationSettings> {
    @Override
    public PublicationSettings getDefaultPropertySet() {
        PublicationSettings s = PropertySetTypeUtil.getEmptyPropertyWrapper(PublicationSettings.class);
        applyDefaultPublicationSettings(s);

        return s;
    }

    public static void applyDefaultPublicationSettings(PublicationSettings s) {
        s.setContentModerationEnabled(true);
        s.setHeaderImageAlignment(HorizontalAlignment.LEFT);
    }
}
