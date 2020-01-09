package org.narrative.network.shared.context;

import org.narrative.network.core.security.area.base.AreaRole;
import org.jetbrains.annotations.NotNull;

/**
 * Date: Dec 21, 2005
 * Time: 10:10:18 AM
 *
 * @author Brian
 */
public interface AreaContextInternal extends AreaContext, NetworkContextInternal {

    public void setAreaRole(@NotNull AreaRole areaRole);

}
