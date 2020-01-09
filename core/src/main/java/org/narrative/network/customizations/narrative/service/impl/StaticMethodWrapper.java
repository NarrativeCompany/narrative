package org.narrative.network.customizations.narrative.service.impl;

import org.narrative.common.util.CoreUtils;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheDAO;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.util.NetworkCoreUtils;
import org.springframework.stereotype.Component;

/**
 * Wrapper that wraps common static methods so that they can be easily mocked for tests.
 * <p>
 * Use of this wrapper will drastically speed up test execution times as well since it removes the need
 * for static mocking which is really expensive.
 * <p>
 * Add your favorites here!
 */
@Component
public class StaticMethodWrapper {
    public NetworkContextImplBase getCurrentNetworkContextImplBase() {
        return NetworkContextImplBase.current();
    }

    public NetworkContext networkContext() {
        return NetworkContextImplBase.current();
    }

    public AreaContext getAreaContext() {
        return NetworkCoreUtils.areaContext();
    }

    public NicheDAO getNicheDAO() {
        return Niche.dao();
    }

    public AreaUserRlm getAreaUserRlmFromUser(User user) {
        AreaUser areaUser = user.getLoneAreaUser().getAreaUser();
        return AreaUser.getAreaUserRlm(areaUser);
    }

    public Area getNarrativePlatformArea() {
        return Area.dao().getNarrativePlatformArea();
    }

    public void checkRegisteredUser() {
        networkContext().getPrimaryRole().checkRegisteredUser();
    }

    public boolean exists(Object o) {
        return CoreUtils.exists(o);
    }
}
