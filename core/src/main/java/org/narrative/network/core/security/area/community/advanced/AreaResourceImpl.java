package org.narrative.network.core.security.area.community.advanced;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;

/**
 * Date: Mar 5, 2009
 * Time: 9:20:17 AM
 *
 * @author brian
 */
public interface AreaResourceImpl {

    public OID getOid();

    public Area getArea();

    public AreaResourceType getAreaResourceType();

    public AreaResource getAreaResource();

    public String getNameForDisplay();

    public Portfolio getPortfolio();
}
