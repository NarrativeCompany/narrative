package org.narrative.network.core.security.area.community.advanced;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.shared.daobase.NetworkDAO;
import org.narrative.network.shared.security.Securable;

/**
 * Date: Mar 5, 2009
 * Time: 9:18:56 AM
 *
 * @author brian
 */
public enum AreaResourceType implements IntegerEnum {
    AREA(0, Area.class, GlobalSecurable.class);

    private final int id;
    private final Class<? extends AreaResourceImpl> areaResourceImplClass;
    private final Class<? extends Securable> securableClass;

    AreaResourceType(int id, Class<? extends AreaResourceImpl> areaResourceImplClass, Class<? extends Securable> securableClass) {
        this.id = id;
        this.areaResourceImplClass = areaResourceImplClass;
        this.securableClass = securableClass;
        assert Enum.class.isAssignableFrom(securableClass) : "Only expecting Securable classes to be Enums!";
    }

    @Override
    public int getId() {
        return id;
    }

    public Class<? extends Securable> getSecurableClass() {
        return securableClass;
    }

    public <T extends Securable> T[] getSecurables() {
        return (T[]) securableClass.getEnumConstants();
    }

    public boolean isSingleton() {
        // bl: currently Area is the only single AreaResourceType
        return isArea();
    }

    public boolean isArea() {
        return this == AREA;
    }

    public NetworkDAO getDAO() {
        // bl: this is a little bit funky, but that's life.  since ContentConsumer doesn't/can't
        // implement DAOObject (since it doesn't have an associated DAO), we need to use an unparameterized
        // call to getDAO to get the generic DAO
        return (NetworkDAO) DAOImpl.getDAO((Class) areaResourceImplClass);
    }

    public AreaResourceImpl getInstance(AreaResource areaResource) {
        return getInstance(areaResource.getOid());
    }

    public AreaResourceImpl getInstance(OID areaResourceOid) {
        // bl: a little bit ugly with a cast since enums can't be parameterized
        return (AreaResourceImpl) getDAO().get(areaResourceOid);
    }

}
