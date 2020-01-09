package org.narrative.network.shared.context;

import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.processes.ActionProcess;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.narrative.network.core.settings.global.services.translations.NetworkResourceBundle;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.security.SystemRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 29, 2005
 * Time: 1:20:38 AM
 */
public abstract class NetworkContextImplBase implements NetworkContext, NetworkContextInternal {

    private static final ThreadLocal<NetworkContextImplBase> currentNetworkContext = new ThreadLocal<>();

    protected final NetworkContextData data;

    // use the default locale if none specified
    protected static class NetworkContextData {
        protected NetworkResourceBundle resourceBundle;
        protected PrimaryRole primaryRole = null;
        protected RequestType requestType = RequestType.CLUSTER_CP; //default to cluster cp
        protected RequestResponseHandler reqResp;
        protected NumberFormat numberFormat;
        protected NumberFormat decimalNumberFormat;
        protected NumberFormat shortDecimalNumberFormat;
        protected final Map<String, Object> contextData = new HashMap<>();
    }

    protected NetworkContextImplBase() {
        this(new NetworkContextData());
    }

    protected NetworkContextImplBase(NetworkContextImplBase networkContextImplBase) {
        this(networkContextImplBase.data);
    }

    private NetworkContextImplBase(NetworkContextData data) {
        this.data = data;
    }

    public static boolean isNetworkContextSet() {
        return currentNetworkContext.get() != null;
    }

    /**
     * Returns the current network context for the local thread
     *
     * @return the current NetworkContextImpl
     */
    @NotNull
    public static NetworkContextImplBase current() {
        assert isNetworkContextSet() : "current() shouldn't be called before a new context is created";
        return currentNetworkContext.get();
    }

    /**
     * Creats a new network context on the current thread
     *
     * @param networkContextImplBase the network context to use
     */
    public static void setCurrentContext(NetworkContextImplBase networkContextImplBase) {
        //assert currentNetworkContext.get() == null : "createRoot() shouldn't be called with a current context in scope";
        currentNetworkContext.set(networkContextImplBase);
    }

    /**
     * Clears the current context for the current thread
     */
    public static void clearCurrent() {
        currentNetworkContext.set(null);
    }

    @NotNull
    public NetworkRegistry getNetworkRegistry() {
        return NetworkRegistry.getInstance();
    }

    /**
     * get the current Locale for this request.  this value should always
     * be in sync with ActionContext.getLocale() (if this is a web request)
     * via the CurrentLocaleInterceptor.
     *
     * @return the current locale for this request.  if no Locale is specified
     * for the current request, then Locale.ENGLISH is returned.
     */
    @NotNull
    public Locale getLocale() {
        if (isHasPrimaryRole()) {
            return getFormatPreferences().getLocale();
        }

        return DefaultLocale.getDefaultLocale();
    }

    @Override
    public void setResourceBundle(NetworkResourceBundle resourceBundle) {
        data.resourceBundle = resourceBundle;
    }

    @NotNull
    public NetworkResourceBundle getResourceBundle() {
        if (data.resourceBundle == null) {
            // bl: adding a default resource bundle now
            // bl: for the default ResourceBundle, let's not include any customization wordlets. those should never be used in this default case.
            data.resourceBundle = NetworkResourceBundle.getDefaultResourceBundle();
        }
        return data.resourceBundle;
    }

    @NotNull
    @Override
    public FormatPreferences getFormatPreferences() {
        if (isHasPrimaryRole()) {
            return getPrimaryRole().getFormatPreferences();
        }
        return FormatPreferences.getDefaultFormatPreferences(getAuthRealm().getDefaultLocale());
    }

    /**
     * Sets the primaryRole on the context.
     *
     * @param primaryRole the PrimaryRole to use
     */
    public void setPrimaryRole(@NotNull PrimaryRole primaryRole) {
        // if we got here we should have some sort of primaryRole by this time
        assert primaryRole != null : "Can't set a null PrimaryRole on the NetworkContext!";
        data.primaryRole = primaryRole;
    }

    @NotNull
    public PrimaryRole getPrimaryRole() {
        assert isHasPrimaryRole() : "Can't get the PrimaryRole on the NetworkContext prior to it being set in an interceptor!";
        return data.primaryRole;
    }

    public boolean isHasPrimaryRole() {
        return data.primaryRole != null;
    }

    public User getUser() {
        PrimaryRole primaryRole = getPrimaryRole();
        if (!primaryRole.isRegisteredUser()) {
            assert !(primaryRole instanceof SystemRole) : "Should never attempt to get the current User off of the NetworkContext when the current role is a SystemRole!  This likely indicates a bug!";
            return null;
        }
        return cast(primaryRole, User.class);
    }

    public boolean isLoggedInUser() {
        return exists(data.primaryRole) && data.primaryRole.isRegisteredUser();
    }

    //    public <T> T doMonitoredTask(GlobalTaskImpl<T> task) {
//        task.doSetNetworkContext(this);
//        return PartitionType.GLOBAL.doMonitoredTask(task, NetworkRegistry.getGlobalPartition());
//    }
//    public <T> T doMonitoredTask(GlobalTaskImpl<T> task, boolean useTransaction) {
//        return PartitionType.GLOBAL.doMonitoredTask(task, NetworkRegistry.getGlobalPartition(), useTransaction);
//    }
//
//    /**
//     * Does a task for the current area.  This creates a new hibernate session, sets
//     * the current area and wraps it in a transaction.
//     *
//     * @param task
//     * @param area
//     * @return
//     */
//    public <T> T doAreaTask(AreaTaskImpl<T> task, Area area) {
//        task.setTaskArea(area);
//        return doAreaTask(task, area, true);
//    }
//
//    /**
//     * Does a task for the current area with the option of not having a transaction.
//     * @param task
//     * @param area
//     * @param useTransaction
//     * @return
//     */
//    public <T> T doAreaTask(Task<T> task, Area area, boolean useTransaction) {
//        assert area != null;
//
//        Area oldArea = currentArea;
//        currentArea = area;
//        try {
//            return PartitionType.REALM.doMonitoredTask(task, area.getRealmPartition(), useTransaction);
//        } finally {
//            currentArea = oldArea;
//        }
//    }
//
//    public <T> T doDialogTask(Task<T> task, DialogCmp dialog) {
//        return doDialogTask(task, dialog);
//    }
//    public <T> T doDialogTask(Task<T> task, DialogCmp dialog, boolean useTransaction) {
//        return PartitionType.COMPOSITION.doMonitoredTask(task, dialog.getCompositionPartition(), useTransaction);
//    }
//
//    public <T> T doContentTask(Task<T> task, Content content) {
//        return doContentTask(task, content, true);
//    }
//
//    public <T> T doContentTask(Task<T> task, Content content, boolean useTransaction) {
//        return PartitionType.COMPOSITION.doMonitoredTask(task, Partition.dao().get(content.getCompositionPartitionOid()), useTransaction);
//    }

    public ActionProcess getActionProcess() {
        return ActionProcess.getActionProcess();
    }

    public RequestType getRequestType() {
        return data.requestType;
    }

    public void setRequestType(RequestType requestType) {
        data.requestType = requestType;
    }

    public RequestResponseHandler getReqResp() {
        return data.reqResp;
    }

    public void setRequestResponse(RequestResponseHandler reqResp) {
        data.reqResp = reqResp;
    }

    public static final String IS_PROCESSING_JSP_EMAIL_CONTEXT_DATA_PARAM = NetworkContextImplBase.class.getSimpleName() + "-IsProcessingJspEmail";

    public boolean isProcessingJspEmail() {
        Boolean isJspEmailProcessingBool = getContextData(NetworkContextImplBase.IS_PROCESSING_JSP_EMAIL_CONTEXT_DATA_PARAM);
        return isJspEmailProcessingBool != null && isJspEmailProcessingBool;
    }

    @Override
    public boolean isUseSecureUrls() {
        // bl: never use secure URLs when processing JSP emails.
        return !isProcessingJspEmail() && data.reqResp != null && data.reqResp.isSecureRequest();
    }

    public void setupSystemRole() {
        assert data.primaryRole == null || data.primaryRole instanceof SystemRole : "Attempted to overwrite a network role with a SystemRole";
        changeRole(new SystemRole());
    }

    @Override
    public void changeRole(PrimaryRole role) {
        data.primaryRole = role;
        // jw: allow the lazy initializers to take care of these if they are used
        data.numberFormat = null;
        data.decimalNumberFormat = null;
        data.shortDecimalNumberFormat = null;
        NetworkResourceBundle bundle = NetworkResourceBundle.getResourceBundle(getAuthRealm(), role);
        setResourceBundle(bundle);
    }

    @Override
    public void runAsPrimaryRole(PrimaryRole primaryRole, Runnable runnable) {
        PrimaryRole originalRole = isHasPrimaryRole() ? getPrimaryRole() : null;
        NetworkResourceBundle originalNetworkResourceBundle = getResourceBundle();
        // bl: setup the supplied role (which probably is not the current user)
        changeRole(primaryRole);
        try {
            runnable.run();
        } finally {
            // once the work is done, change the current user back to what it was previously
            changeRole(originalRole);
            setResourceBundle(originalNetworkResourceBundle);
        }
    }

    public String formatNumber(long val) {
        // bl: cache the NumberFormat for number formatting purposes on the NetworkContext so that we don't
        // have to look it up each time.
        if (data.numberFormat == null) {
            data.numberFormat = NumberFormat.getIntegerInstance(getLocale());
        }
        return data.numberFormat.format(val);
    }

    public String formatDecimalNumber(double val) {
        // bl: cache the NumberFormat for number formatting purposes on the NetworkContext so that we don't
        // have to look it up each time.
        if (data.decimalNumberFormat == null) {
            data.decimalNumberFormat = NumberFormat.getNumberInstance(getLocale());
            // bl: the chat push statistic has a value of 0.0008. by default, the NumberFormat returned here
            // has a maximumFractionDigits set to 3. let's set it to 4 to ensure we display the amount correctly.
            if (data.decimalNumberFormat.getMaximumFractionDigits() < 4) {
                data.decimalNumberFormat.setMaximumFractionDigits(4);
            }
        }
        return data.decimalNumberFormat.format(val);
    }

    public String formatShortDecimalNumber(double val) {
        // bl: cache the NumberFormat for number formatting purposes on the NetworkContext so that we don't
        // have to look it up each time.
        if (data.shortDecimalNumberFormat == null) {
            data.shortDecimalNumberFormat = NumberFormat.getNumberInstance(getLocale());
            // jw: lets ensure that the maximum digits is 2 for the short decimal format
            if (data.shortDecimalNumberFormat.getMaximumFractionDigits() != 2) {
                data.shortDecimalNumberFormat.setMaximumFractionDigits(2);
            }
        }
        return data.shortDecimalNumberFormat.format(val);
    }

    @NotNull
    public GSession getGlobalSession() {
        return PartitionType.GLOBAL.currentSession();
    }

    @Override
    public boolean isGlobalSessionAvailable() {
        return PartitionType.GLOBAL.hasCurrentSession();
    }

    public final <T> T getContextData(String key) {
        return (T) data.contextData.get(key);
    }

    public final void setContextData(String key, Object obj) {
        if (obj == null) {
            data.contextData.remove(key);
        } else {
            data.contextData.put(key, obj);
        }
    }

    @Override
    public void reassociateCachedContextAfterSessionClear() {
        if (data.primaryRole != null && isOfType(data.primaryRole, User.class)) {
            data.primaryRole = User.dao().get(data.primaryRole.getOid());
        }
    }

    @Override
    public <T> T doAuthZoneTask(final AuthZone authZone, final GlobalTaskImpl<T> task) {
        assert isEqual(authZone, getAuthZone()) || getAuthZone() == null : "Should only attempt AuthZone tasks in the same AuthZone as currently running - or an AuthZone task from the context of the Network AuthZone currentAz/" + getAuthZone() + " newAz/" + authZone;
        // bl: if no authZone is supplied, then just do it as a global task.
        if (authZone == null) {
            return doGlobalTask(task);
        }
        return doAreaTask(authZone.getArea(), new AreaTaskImpl<T>(task.isForceWritable()) {
            @Override
            protected T doMonitoredTask() {
                assert isEqual(authZone, getAreaContext().getAuthZone()) : "Should always be in the context of the target AuthZone when doing an AuthZone task!";
                return getNetworkContext().doGlobalTask(task);
            }
        });
    }

    /**
     * Runs a network task under this context
     *
     * @param task that task to do
     * @return the value returned from the specified GlobalTaskImpl
     */
    public <T> T doGlobalTask(GlobalTaskImpl<T> task) {
        return TaskRunner.doGlobalTask(this, task);
    }

    public <T> T doGlobalTask(TaskOptions taskOptions, GlobalTaskImpl<T> task) {
        return TaskRunner.doGlobalTask(this, taskOptions, task);
    }

    /**
     * Runs an area task under this context, given an area and an areaTask.  Current role must be an area role.
     * <p>
     * By default, this will run the AreaTaskImpl in a new session.
     *
     * @param area the area to do the task in
     * @param task the task to do
     * @return the value returned from the specified AreaTaskImpl
     */
    public <T> T doAreaTask(Area area, AreaTaskImpl<T> task) {
        return doAreaTask(area, new TaskOptions(), task);
    }

    public <T> T doAreaTask(Area area, TaskOptions taskOptions, AreaTaskImpl<T> task) {
        assert isEqual(area.getAuthZone(), getAuthZone()) || getAuthZone() == null : "Should only attempt Area tasks in the same AuthZone as currently running - or an AuthZone task from the context of the Network AuthZone. currentAz/" + getAuthZone() + " area/" + area.getOid() + " areaAz/" + area.getAuthZone();
        NetworkContextImplBase original = current();
        assert original.data == data : "Can't call doAreaTask when the internal data is different from the current NetworkContext.  Not sure how this could ever even happen! taskClass/" + task.getClass().getName();

        // determine if we can run under the current AreaContext
        if (original instanceof AreaContextImpl) {
            AreaContextImpl currentAreaContext = (AreaContextImpl) original;
            // if we're using the existing session (which by definition must still be open if there is an AreaContext
            // set) and the current area context is for the specified area, then we can do an optimization
            // here to re-use the AreaContext for the new task.
            // nb. no pre-task hook necessary here since the AreaRole must have already been properly set up.
            if (!taskOptions.getTaskIsolationLevel().isIsolated() && IPUtil.isEqual(currentAreaContext.getArea().getOid(), area.getOid())) {
                return TaskRunner.doAreaTask(currentAreaContext, taskOptions, task);
            }
        }

        // prior to actually doing the task, we need to set the AreaRole on the AreaContextImpl.
        // we must do this after the Realm Hibernate session has been established.  thus,
        // we must use a "hook" mechanism into PartitionType.doTask() to allow some arbitrary
        // code (a Runnable) to be executed prior to doing the task.
//pm: this is now being done just in time in the AreaContext.getAreaRole()            
//            taskOptions.addPreTaskHook(new Runnable() {
//                public void run() {
//                    areaContextImpl.setAreaRoleFromPrimaryRoleIfPossible();
//                }
//            });
        return TaskRunner.doAreaTask(new AreaContextImpl(this, area), taskOptions, task);
    }

    public <T> T doCompositionTask(Partition compositionPartition, CompositionTaskImpl<T> compositionTask) {
        return doCompositionTask(compositionPartition, new TaskOptions(), compositionTask);
    }

    public <T> T doCompositionTask(Partition compositionPartition, TaskOptions taskOptions, CompositionTaskImpl<T> compositionTask) {
        return TaskRunner.doCompositionTask(this, compositionPartition, taskOptions, compositionTask);
    }

}
