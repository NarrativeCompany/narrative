package org.narrative.network.core.security.area.community.advanced.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaCirclePermission;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.AreaResourceType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.narrative.network.shared.security.Securable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 5, 2009
 * Time: 8:58:15 AM
 *
 * @author brian
 */
public class AreaCirclePermissionDAO extends GlobalDAOImpl<AreaCirclePermission, OID> {
    public AreaCirclePermissionDAO() {
        super(AreaCirclePermission.class);
    }

    public int getCountWithPermissionForAreaResource(AreaResource areaResource, Securable securable, Collection<OID> includeAreaUserOids) {
        assert isEqual(areaResource.getAreaResourceType(), securable.getAreaResourceType()) : "Mis-match of AreaResourceTypes!";

        // jw: to simplify the HQL, lets just make sure we always have a value in the collection (without modifying the one that was passed in)
        if (isEmptyOrNull(includeAreaUserOids)) {
            includeAreaUserOids = Collections.singleton(OID.DUMMY_OID);
        }

        return ((Number) getGSession().getNamedQuery("areaCirclePermission.getCountWithPermissionForAreaResource").setParameter("area", areaResource.getArea()).setParameter("areaResourceOid", areaResource.getOid()).setParameter("securableType", securable.getId()).setParameterList("includeAreaUserOids", includeAreaUserOids).uniqueResult()).intValue();
    }

    public List<OID> getAreaUserOidsWithPermissionForAreaResource(AreaResource areaResource, Securable securable, Collection<OID> excludeAreaUserOids) {
        assert isEqual(areaResource.getAreaResourceType(), securable.getAreaResourceType()) : "Mis-match of AreaResourceTypes!";

        // jw: to simplify the HQL, lets just make sure we always have a value in the collection (without modifying the one that was passed in)
        if (isEmptyOrNull(excludeAreaUserOids)) {
            excludeAreaUserOids = Collections.singleton(OID.DUMMY_OID);
        }

        return getGSession().getNamedQuery("areaCirclePermission.getAreaUserOidsWithPermissionForAreaResource").setParameter("area", areaResource.getArea()).setParameter("areaResourceOid", areaResource.getOid()).setParameter("securableType", securable.getId()).setParameterList("excludeAreaUserOids", excludeAreaUserOids).list();
    }

    public int deleteInvalidPermissions(AreaResourceType resourceType) {
        Securable[] securables = resourceType.getSecurables();

        List<Integer> securableTypes = new ArrayList<>(securables.length);
        for (Securable securable : securables) {
            securableTypes.add(securable.getId());
        }

        return getGSession().getNamedQuery("areaCirclePermission.deleteInvalidPermissions").setParameter("areaResourceType", resourceType.getId()).setParameterList("securableTypes", securableTypes).executeUpdate();
    }
}
