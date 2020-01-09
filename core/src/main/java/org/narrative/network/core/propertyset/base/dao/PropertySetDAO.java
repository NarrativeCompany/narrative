package org.narrative.network.core.propertyset.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.Query;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:00:44 PM
 */
public class PropertySetDAO extends GlobalDAOImpl<PropertySet, OID> {
    public PropertySetDAO() {
        super(PropertySet.class);
    }

    private static final Map<PropertySetType, OID> PROPERTY_SET_TYPE_TO_OID = newHashMap();

    public void initializeDefaultPropertSets() {
        assert PROPERTY_SET_TYPE_TO_OID.isEmpty() : "Should only attempt to initialize default propery sets once!";
        for (PropertySet propertySet : getAll()) {
            PROPERTY_SET_TYPE_TO_OID.put(propertySet.getPropertySetType(), propertySet.getOid());
        }
    }

    public static void init() {

    }

    public static void invalidatePropertySet(PropertySet propertySet) {

    }

    public PropertySet getDefaultPropertySetByType(PropertySetType propertySetType) {
        OID oid;
        // bl: don't worry about doing this optimization during install since the initialization won't actually happen.
        if (NetworkRegistry.getInstance().isInitDone() && !NetworkRegistry.getInstance().isInstalling()) {
            oid = PROPERTY_SET_TYPE_TO_OID.get(propertySetType);
            assert oid != null : "Failed lookup of default PropertySet OID after initialization! Shouldn't be possible! type/" + propertySetType;
        } else {
            // get the property set oid and then get from map. saves on population of objects when doing lookups
            // by name.  might be preferrable.  or make this not cacheable and do lookups a different way.
            Query query = getGSession().getNamedQuery("propertySet.getDefaultPropertySetByType");
            query.setParameter("propertySetType", propertySetType);
            query.setCacheable(true);
            oid = (OID) query.uniqueResult();
        }
        return get(oid);
    }


}
