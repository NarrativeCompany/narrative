package org.narrative.network.shared.context;

import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.shared.security.ClusterRole;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.security.SystemRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 1:18:10 PM
 */
public class AreaContextImpl extends NetworkContextImplBase implements AreaContext, AreaContextInternal {

    private Area area;
    private AreaRole areaRole;
    private AreaUser areaUser;

    public AreaContextImpl(NetworkContextImplBase networkContextImplBase, @NotNull Area area) {
        super(networkContextImplBase);
        this.area = area;
        // bl: displayResource and styleSettings will be set on the fly via interceptors
    }

    /**
     * get the base URL, which in the case of an AreaContextImpl,
     * is just the area's primary area url.
     *
     * @return the area's primary area url.
     */
    @NotNull
    public String getBaseUrl() {
        return area.getPrimaryAreaUrl();
    }

    @NotNull
    public Area getArea() {
        return area;
    }

    @NotNull
    public AreaRlm getAreaRlm() {
        return Area.getAreaRlm(area);
    }

    public void setAreaRoleFromPrimaryRoleIfPossible() {
        // only set the AreaRole if a PrimaryRole has already been set on the NetworkContext.
        // accessing field directly since getPrimaryRole() requires the PrimaryRole already have
        // been set, which it hasn't necessarily at this point.
        if (data.primaryRole != null && !(data.primaryRole instanceof SystemRole) && !(data.primaryRole instanceof ClusterRole)) {
            setAreaRole(data.primaryRole.getAreaRoleForArea(area));
        }
    }

    public void setAreaRole(@NotNull AreaRole areaRole) {
        assert this.areaRole == null : "Can't set the AreaRole after it has already been set!";

        setAreaRoleInternal(areaRole);
    }

    private void setAreaRoleInternal(@NotNull AreaRole areaRole) {
        this.areaRole = areaRole;
        if (isOfType(areaRole, AreaUser.class)) {
            this.areaUser = cast(areaRole, AreaUser.class);
        }
    }

    public AreaRole getAreaRole() {
        if (areaRole == null) {
            setAreaRoleFromPrimaryRoleIfPossible();
        }
        return areaRole;
    }

    @Nullable
    public AreaUser getAreaUser() {
        getAreaRole();
        return areaUser;
    }

    public AreaUserRlm getAreaUserRlm() {
        getAreaRole();
        if (exists(areaUser)) {
            return AreaUser.getAreaUserRlm(areaUser);
        } else {
            return null;
        }
    }

    @Override
    public void changeRole(PrimaryRole role) {
        super.changeRole(role);
        // bl: clear the areaRole and areaUser. we'll need to reload their values if there are
        // any subsequent calls to getAreaRole, getAreaUser, or getAreaUserRlm.
        areaRole = null;
        areaUser = null;
    }

    private transient Portfolio portfolio;

    @NotNull
    @Override
    public Portfolio getPortfolio() {
        if (!exists(portfolio)) {
            portfolio = getAreaRlm().getDefaultPortfolio();
        }
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    /**
     * do an area task from the context of the current realm session
     *
     * @param areaTask the area task to do
     * @return the value returned from the execution of the AreaTaskImpl
     */
    public <T> T doAreaTask(AreaTaskImpl<T> areaTask) {
        return TaskRunner.doAreaTask(this, areaTask);
    }

    public <T> T doAreaTask(TaskOptions taskOptions, AreaTaskImpl<T> areaTask) {
        // bl: if we're doing the specified task in a new session,
        // we can't re-use the current AreaContext since the AreaRlm and AreaUserRlm will
        // need to be re-read for the new Hibernate session that is created.
        // thus, instead of duplication logic here, let's just delegate to the
        // NetworkContext.doAreaTask() to determine whether or not to re-use
        // this AreaContext.
        return doAreaTask(area, taskOptions, areaTask);
    }

    @Override
    public AuthRealm getAuthRealm() {
        return getAuthZone();
    }

    @Override
    public AuthZone getAuthZone() {
        return area.getAuthZone();
    }

    @NotNull
    @Override
    public FormatPreferences getFormatPreferences() {
        AreaRole areaRole = getAreaRole();
        if (areaRole != null) {
            return areaRole.getFormatPreferences();
        }
        // jw: for AreaContext we want to make sure that we use the AreaRoles format preferences in case this is a guest on a group.
        return super.getFormatPreferences();
    }

    @Override
    public void reassociateCachedContextAfterSessionClear() {
        super.reassociateCachedContextAfterSessionClear();
        area = Area.dao().get(area.getOid());

        //jw: Another occurrence of the annoying "collection was not processed by flush()"
        HibernateUtil.initializeObject(area);
        if (areaUser != null) {
            final AreaUser areaUserLocal;
            setAreaRoleInternal(areaUserLocal = AreaUser.dao().get(areaUser.getOid()));
            HibernateUtil.initializeObject(areaUserLocal);
        }
        if (portfolio != null) {
            portfolio = Portfolio.dao().get(portfolio.getOid());
        }
    }

    private boolean skipWidgetTaskExecution = false;

}
