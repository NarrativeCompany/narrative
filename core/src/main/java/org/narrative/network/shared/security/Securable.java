package org.narrative.network.shared.security;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.AreaResourceImpl;
import org.narrative.network.core.security.area.community.advanced.AreaResourceType;
import org.narrative.network.core.user.AuthZone;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 8, 2006
 * Time: 10:29:09 AM
 */
public interface Securable<T extends AreaResourceImpl> extends IntegerEnum {

    public int getOrdinal();

    public boolean hasRight(AreaRole role);

    public void checkRight(AreaRole role);

    public String getNameForDisplay();

    public String getDescription();

    public AreaResourceType getAreaResourceType();

    public boolean isAdminSecurable();

    public boolean isValidForAreaResource(T areaResourceImpl);

    public boolean isRequiredForAreaCircle(AreaCircle areaCircle);

    public boolean hasRight(AuthZone authZone, Set<Securable> securables);

    public boolean hasRight(T areaResourceImpl, AreaRole areaRole);

    public boolean hasRight(AreaResource areaResource, AreaRole areaRole);

    public void checkRight(T areaResourceImpl, AreaRole areaRole);

    public void checkRight(AreaResource areaResourceImpl, AreaRole areaRole);

    public static boolean isSecurableRequired(AreaCircle areaCircle, Securable securable) {
        return securable.isRequiredForAreaCircle(areaCircle);
    }
}
