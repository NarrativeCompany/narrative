package org.narrative.network.core.propertyset.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.propertyset.area.AreaPropertyOverride;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.PropertyType;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 9, 2005
 * Time: 11:04:18 AM
 *
 * @author Brian
 */
public class InstallDefaultPropertySets extends GlobalTaskImpl<Object> {

    public InstallDefaultPropertySets() {}

    protected Object doMonitoredTask() {
        final Map<PropertySetType, Set<String>> areaPropertyOverridesToRemove = new HashMap<>();

        for (Class<? extends PropertySetTypeBase> propertySetTypeDefClass : NetworkRegistry.PROPERTY_SET_TYPE_DEFS) {
            PropertySetType propertySetType = PropertySetType.getPropertySetTypeByInterface(propertySetTypeDefClass);
            PropertySetTypeDef defAnnotation = propertySetTypeDefClass.getAnnotation(PropertySetTypeDef.class);
            PropertySetTypeDefaultValueProvider<? extends PropertySetTypeBase> defaultProvider = null;
            try {
                defaultProvider = defAnnotation.defaultProvider().newInstance();
            } catch (Throwable t) {
                assert false : "All PropertySetTypeDefaultValueProvider impl must provide a default, public constructor!";
            }

            PropertySet propertySet = PropertySet.dao().getDefaultPropertySetByType(propertySetType);
            boolean isNewPropertySet = false;
            if (!exists(propertySet)) {
                propertySet = new PropertySet(propertySetType);
                isNewPropertySet = true;
            }
            PropertyMap propertyMap = defaultProvider.getDefaultPropertySet().wrappedPropertyMap();
            for (PropertyType propertyType : propertySetType.getPropertyTypes().values()) {
                //if global and already specified, don't overwrite
                if (propertySetType.isGlobal() && propertySet.getPropertyValueByName(propertyType.getName()) != null) {
                    continue;
                }
                String defaultValue = propertyMap.getPropertyValueByName(propertyType.getName());
                propertySet.setPropertyValue(propertyType, defaultValue);
            }
            // bl: see if there are any default properties to remove from existing sets
            if (!isNewPropertySet) {
                Iterator<String> iter = propertySet.getPropertyTypeToPropertyInited().keySet().iterator();
                while (iter.hasNext()) {
                    String existingPropertyName = iter.next();
                    // didn't find the existing property name that was in the database in the default
                    // property set type map?  then it must no longer be a valid property, in which
                    // case we can go ahead and remove it from the map.
                    if (!propertySetType.getPropertyTypes().containsKey(existingPropertyName)) {
                        Property property = propertySet.getPropertyByName(existingPropertyName);
                        iter.remove();
                        Property.dao().delete(property);
                        if (!propertySetType.isGlobal()) {
                            addMapSetLookupValue(areaPropertyOverridesToRemove, propertySetType, existingPropertyName);
                        }
                    }
                }
            }
            // save or update the property set, which will cascade to all properties therein.
            PropertySet.dao().saveOrUpdate(propertySet);
        }

        // todo: this doesn't handle removing AreaPropertyOverrides that don't have default values (since that's the only
        // way that we identify properties that have been removed). thus, we likely have a lot of orphaned properties
        // in AreaPropertyOverride that have been removed and are no longer supported. one such example is customCodeAboveSignInBarHtml.
        // bl: now remove any AreaPropertyOverrides necessary
        if (!areaPropertyOverridesToRemove.isEmpty()) {
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(true) {
                protected Object doMonitoredTask() {
                    for (Map.Entry<PropertySetType, Set<String>> removeEntry : areaPropertyOverridesToRemove.entrySet()) {
                        Set<String> overridesToRemove = removeEntry.getValue();

                        List<OID> areaPropertSetOids = AreaPropertyOverride.dao().getAreaPropertySetOidsWithAreaPropertyOverrides(removeEntry.getKey(), overridesToRemove);

                        SubListIterator<OID> areaPropertySetOidChunks = new SubListIterator<OID>(areaPropertSetOids, 100);
                        while (areaPropertySetOidChunks.hasNext()) {
                            List<OID> oids = areaPropertySetOidChunks.next();
                            List<AreaPropertySet> propertySets = AreaPropertySet.dao().getObjectsFromIDs(oids);
                            for (AreaPropertySet propertySet : propertySets) {
                                for (String areaPropertyOverrideToRemove : overridesToRemove) {
                                    AreaPropertyOverride apo = propertySet.getPropertyOverridesInited().remove(areaPropertyOverrideToRemove);
                                    if (apo != null) {
                                        AreaPropertyOverride.dao().delete(apo);
                                    }
                                }
                            }
                            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();
                            PartitionGroup.getCurrentPartitionGroup().clearAllSessions();
                        }
                    }
                    return null;
                }
            });
        }

        return null;
    }

    public static void initializeDefaultPropertySets() {
        TaskRunner.doRootGlobalTask(new InstallDefaultPropertySets());
    }
}
