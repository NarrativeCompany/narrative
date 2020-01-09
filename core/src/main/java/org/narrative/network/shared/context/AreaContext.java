package org.narrative.network.shared.context;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 10:43:27 AM
 */
public interface AreaContext extends NetworkContext {
    @NotNull
    public Area getArea();

    @NotNull
    public AreaRlm getAreaRlm();

    public AreaRole getAreaRole();

    @Nullable
    public AreaUser getAreaUser();

    @Nullable
    public AreaUserRlm getAreaUserRlm();

    @NotNull
    public Portfolio getPortfolio();

    /**
     * do an area task from the context of the current realm session.
     * to execute an AreaTask in a new session, you should use
     * NetworkContext.doAreaTask().
     *
     * @param areaTask the area task to do
     * @return the value returned from the execution of the AreaTaskImpl
     */
    public <T> T doAreaTask(AreaTaskImpl<T> areaTask);

    public <T> T doAreaTask(TaskOptions taskOptions, AreaTaskImpl<T> areaTask);

}
