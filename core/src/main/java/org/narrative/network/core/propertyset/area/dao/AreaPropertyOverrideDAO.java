package org.narrative.network.core.propertyset.area.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.propertyset.area.AreaPropertyOverride;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;

/**
 * Date: Dec 2, 2005
 * Time: 4:26:39 PM
 *
 * @author Brian
 */
public class AreaPropertyOverrideDAO extends GlobalDAOImpl<AreaPropertyOverride, OID> {
    public AreaPropertyOverrideDAO() {
        super(AreaPropertyOverride.class);
    }

    @Override
    public AreaPropertyOverride get(OID oid) {
        // bl: use getObject so that we don't get a proxy here.  we want a fully
        // instantiated and populated AreaPropertyOverride object.
        return getReal(oid);
    }

    public List<OID> getAreaPropertySetOidsWithAreaPropertyOverrides(PropertySetType propertySetType, Collection<String> propertyTypesToFind) {
        return getGSession().getNamedQuery("areaPropertyOverride.areaPropertySetOidsWithOverridesOfPropertyTypes").setParameter("propertySetType", propertySetType).setParameterList("propertyTypes", propertyTypesToFind).list();
    }
}
