package org.narrative.network.core.settings.global.services;

import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;
import org.narrative.network.core.settings.global.GlobalSettings;

/**
 * Date: Dec 7, 2005
 * Time: 3:45:18 PM
 *
 * @author Brian
 */
public class GlobalSettingsUtil {
    /**
     * get an instance of a global settings class from the interface
     *
     * @param globalSettingsClass the interface containing the global settings
     * @param readOnly
     * @return the lone instance of a given set of global settings
     */
    public static <T extends PropertySetTypeBase> T getSettings(Class<T> globalSettingsClass, boolean readOnly) {
        assert globalSettingsClass.getAnnotation(PropertySetTypeDef.class).global() : "Must specify an interface class that has the global attribute set to true on the PropertySetTypeDef annotation! Invalid class: " + globalSettingsClass;
        PropertySetType globalType = PropertySetType.getPropertySetTypeByInterface(globalSettingsClass);
        PropertySet globalPropertySet = globalType.getDefaultPropertySet();
        return PropertySetTypeUtil.getPropertyWrapper(globalSettingsClass, globalPropertySet);
    }

    public static GlobalSettings getGlobalSettings() {
        return getSettings(GlobalSettings.class, true);
    }

    public static GlobalSettings getGlobalSettingsForWrite() {
        return getSettings(GlobalSettings.class, false);
    }
}
