package org.narrative.network.core.versioning.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.AppVersion;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 31, 2008
 * Time: 2:25:47 PM
 */
public class AppVersionDAO extends GlobalDAOImpl<AppVersion, OID> {
    public AppVersionDAO() {
        super(AppVersion.class);
    }

    private static final String SELECT_LAST_VERSION_SQL;
    private static final String SELECT_APP_VERSION_BY_VERSION_SQL;

    static {
        {
            StringBuilder sql = new StringBuilder();
            sql.append("select ");
            sql.append(AppVersion.FIELD__VERSION__COLUMN);
            sql.append(", ");
            sql.append(AppVersion.FIELD__COMPLETE_DATETIME__COLUMN);
            sql.append(" from ");
            sql.append(AppVersion.class.getSimpleName());
            sql.append(" order by ");
            sql.append(AppVersion.FIELD__START_DATETIME__COLUMN);
            sql.append(" desc limit 1");
            SELECT_LAST_VERSION_SQL = sql.toString();
        }
        {
            StringBuilder sql = new StringBuilder();
            sql.append("select 1 from ");
            sql.append(AppVersion.class.getSimpleName());
            sql.append(" where version = ? limit 1");
            SELECT_APP_VERSION_BY_VERSION_SQL = sql.toString();
        }
    }

    public AppVersion getLastVersion() {
        return (AppVersion) getGSession().getNamedQuery("appVersion.getLatestAppVersion").setMaxResults(1).uniqueResult();
    }

    public AppVersion getByVersion(String version) {
        return getUniqueBy(new NameValuePair<>(AppVersion.FIELD__VERSION__NAME, version));
    }

    public static Boolean isUpToDateForBootstrap(String version) {
        ResultSet rs = null;
        try {
            DatabaseResources dr = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
            String lastVersion;
            Timestamp completeDatetime;
            try {
                rs = dr.getResultSet(SELECT_LAST_VERSION_SQL);
                // no last version?  maybe we're starting up a new environment, so assume not up to date.
                // probably shouldn't be possible since install should install an AppVersion, but leaving
                // this way for completeness.
                if (!rs.next()) {
                    return false;
                }
                lastVersion = rs.getString(1);
                completeDatetime = rs.getTimestamp(2);
            } finally {
                PersistenceUtil.close(rs);
            }

            // if the currently running version matches the last version run, then we might be up to date!
            if (version.equals(lastVersion)) {
                // bl: we are only up to date if the completeDatetime is non-null.  if it's null, then we need to
                // continue the patch run where we left off since it must have broken in the middle.
                return completeDatetime != null;
            }
            // if the versions don't match, then check to see if we had previously run the current version
            // which would indicate that we are now using an older version of the code (which is an error condition)
            Number count = (Number) dr.executeScalarStatement(SELECT_APP_VERSION_BY_VERSION_SQL, Arrays.asList(version));
            if (count != null) {
                // bl: if we have previously run, then we're running an older version.
                throw UnexpectedError.getRuntimeException("Looks like you are trying to run an old version of the code! This is not supported! version/" + version + " lastVersion/" + lastVersion);
            }
            // the current version we are trying to run has not been run before?  then we're not up to date
            // and we should run the bootstrap patches!
            return false;
        } catch (Throwable t) {
            // can't determine if we need to run based on the AppVersion table - perhaps bootstrap patch
            // needs to be run against the table, so return null to indicate that we can't tell.
            return null;
        } finally {
            // bl: in case the patch fails, call onEndOfThread to clear the thread in error flag.
            // bl: onEndOfThread will also cleanup the bootstrap DatabaseResources
            IPUtil.onEndOfThread();
        }
    }

    public static boolean isUpToDate(final String version) {
        // eventually we might want to have an exception for dev servers to not worry about the appVersion, since
        // they will get a new app version on every startup.  But keeping it in for now since we want as much testing as possible
        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Boolean>() {
            protected Boolean doMonitoredTask() {
                AppVersion lastVersion = AppVersion.dao().getLastVersion();

                // no last version? then we're not up to date.
                if (!exists(lastVersion)) {
                    return false;
                }

                // last version matches the current version?  then we might be up to date!
                if (lastVersion.getVersion().equals(version)) {
                    // bl: we are only up to date if the completeDatetime is non-null.  if it's null, then we need to
                    // continue the patch run where we left off since it must have broken in the middle.
                    return lastVersion.getCompleteDatetime() != null;
                }

                // bl: just in case we try to run an older version of the code, detect that scenario here and throw
                // an exception preventing startup.  this would have helped recently when we started up an old
                // version of fusion on the production database, which ultimately caused some patches to be run
                // that mucked with data in the database.  had we had this check in place, we could have avoided
                // that scenario.
                AppVersion sameVersion = AppVersion.dao().getByVersion(version);
                if (exists(sameVersion)) {
                    // bl: in order to prevent race conditions on the dv01 server (due to auto-update), let's do one more
                    // lookup here to see if possibly the last version was just updated. if it was, then we can treat as being up-to-date.
                    lastVersion = AppVersion.dao().getLastVersion();
                    if (exists(lastVersion) && lastVersion.getVersion().equals(version)) {
                        return lastVersion.getCompleteDatetime() != null;
                    }
                    // bl: if we have previously run, then we're running an older version.
                    throw UnexpectedError.getRuntimeException("Looks like you are trying to run an old version of the code! This is not supported! version/" + version + " lastVersion/" + lastVersion.getVersion());
                }

                // must be up to date since the last version is the current version!
                return false;
            }
        });
    }
}