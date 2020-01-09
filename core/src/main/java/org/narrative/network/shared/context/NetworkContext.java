package org.narrative.network.shared.context;

import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.processes.ActionProcess;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.settings.global.services.translations.NetworkResourceBundle;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 10:42:00 AM
 */
public interface NetworkContext {

    @NotNull
    public NetworkRegistry getNetworkRegistry();

    @NotNull
    public Locale getLocale();

    @NotNull
    public NetworkResourceBundle getResourceBundle();

    @NotNull
    public FormatPreferences getFormatPreferences();

    @NotNull
    public PrimaryRole getPrimaryRole();

    public boolean isHasPrimaryRole();

    public User getUser();

    public boolean isLoggedInUser();

    public boolean isProcessingJspEmail();

    @NotNull
    public GSession getGlobalSession();

    public boolean isGlobalSessionAvailable();

    public String formatNumber(long val);

    public String formatDecimalNumber(double val);

    public String formatShortDecimalNumber(double val);

    public <T> T doAuthZoneTask(AuthZone authZone, GlobalTaskImpl<T> task);

    public <T> T doGlobalTask(GlobalTaskImpl<T> task);

    public <T> T doGlobalTask(TaskOptions taskOptions, GlobalTaskImpl<T> task);

    public <T> T doAreaTask(Area area, AreaTaskImpl<T> task);

    public <T> T doAreaTask(Area area, TaskOptions taskOptions, AreaTaskImpl<T> task);

    /**
     * do a composition task
     *
     * @param compositionPartition the composition partition on which to do the task
     * @param compositionTask      the task to do
     * @return the result of the composition task
     */
    public <T> T doCompositionTask(Partition compositionPartition, CompositionTaskImpl<T> compositionTask);

    public <T> T doCompositionTask(Partition compositionPartition, TaskOptions taskOptions, CompositionTaskImpl<T> compositionTask);

    void runAsPrimaryRole(PrimaryRole primaryRole, Runnable runnable);

    @NotNull
    public String getBaseUrl();

    public ActionProcess getActionProcess();

    public RequestType getRequestType();

    public RequestResponseHandler getReqResp();

    public <T> T getContextData(String key);

    public AuthRealm getAuthRealm();

    public AuthZone getAuthZone();

    public boolean isUseSecureUrls();

}

