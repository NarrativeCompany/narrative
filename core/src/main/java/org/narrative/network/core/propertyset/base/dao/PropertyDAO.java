package org.narrative.network.core.propertyset.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:00:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyDAO extends GlobalDAOImpl<Property, OID> {
    public PropertyDAO() {
        super(Property.class);
    }

    @Override
    public Property get(OID oid) {
        // bl: use getObject so that we don't get a proxy here.  we want a fully
        // instantiated and populated Property object.
        return getReal(oid);
    }

    public <T extends PropertySetTypeBase> void lockProperty(Class<T> propertySetTypeInterface, String propertyType) {
        PropertySet propertySet = PropertySetType.getPropertySetTypeByInterface(propertySetTypeInterface).getDefaultPropertySet();
        Property property = propertySet.getPropertyByName(propertyType);
        assert exists(property) : "We should always have a property when calling this method!";

        lock(property);
    }
}
