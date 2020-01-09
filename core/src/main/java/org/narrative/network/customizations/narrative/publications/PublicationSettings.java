package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.web.HorizontalAlignment;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.propertyset.base.services.annotations.IsDefaultRequired;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;

import java.util.HashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 08:54
 *
 * @author jonmark
 */
@PropertySetTypeDef(name = PublicationSettings.NAME, defaultProvider = DefaultPublicationSettings.class)
public abstract class PublicationSettings implements PropertySetTypeBase {
    public static final String NAME = "PublicationSettings";

    public abstract boolean isContentModerationEnabled();
    public abstract void setContentModerationEnabled(boolean enabled);

    @IsDefaultRequired(false)
    public abstract String getFathomSiteId();
    public abstract void setFathomSiteId(String id);

    public abstract HorizontalAlignment getHeaderImageAlignment();
    public abstract void setHeaderImageAlignment(HorizontalAlignment alignment);

    @IsDefaultRequired(false)
    public abstract Map<PublicationUrlType, String> getUrlsByType();
    public abstract void setUrlsByType(Map<PublicationUrlType, String> lookup);

    public String getUrl(PublicationUrlType type) {
        Map<PublicationUrlType, String> lookup = getUrlsByType();

        return lookup == null ? null : lookup.get(type);
    }
}
