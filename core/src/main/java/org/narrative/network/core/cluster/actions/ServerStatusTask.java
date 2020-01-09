package org.narrative.network.core.cluster.actions;

import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.RuntimeUtils;
import org.narrative.common.util.processes.ActionProcess;
import org.narrative.common.util.processes.GenericHistory;
import org.narrative.common.util.processes.HistoryManager;
import org.narrative.common.util.processes.RecentData;
import org.narrative.common.util.processes.RequestProcessBase;
import org.narrative.common.util.processes.SpringProcess;
import org.narrative.network.core.system.HeartbeatServer;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 22, 2007
 * Time: 2:41:18 PM
 */
public class ServerStatusTask extends GlobalTaskImpl<Object> {

    private static final NetworkLogger logger = new NetworkLogger(ServerStatusTask.class);

    private final Stats total = new Stats();
    private final Stats recent = new Stats();

    private long servletStartTime;

    private long uptimeMinutes;
    private String unixUptime = null;

    public ServerStatusTask() {}

    public class Stats {
        private DecimalFormat formatter = new DecimalFormat("##########.00");
        public String avgRequestsSec;
        public String avgResponseTime;
        public long requests;
        public long requestTime;

        public void format(double seconds) {
            if (requests > 0) {
                avgResponseTime = formatter.format((double) requestTime / (double) requests);
                avgRequestsSec = formatter.format((double) requests / seconds);
            } else {
                avgResponseTime = "0.00";
                avgRequestsSec = "0.00";
            }
        }

        public String getAvgRequestsSec() {
            return avgRequestsSec;
        }

        public String getAvgResponseTime() {
            return avgResponseTime;
        }

        public long getRequests() {
            return requests;
        }

        public long getRequestTime() {
            return requestTime;
        }
    }

    public boolean isForceWritable() {
        return false;
    }

    protected Object doMonitoredTask() {

        servletStartTime = NetworkRegistry.getInstance().getServerStartTime();
        uptimeMinutes = (long) (((double) (System.currentTimeMillis() - servletStartTime)) / (double) IPDateUtil.MINUTE_IN_MS);

        for (Class<? extends RequestProcessBase> cls : Arrays.asList(ActionProcess.class, SpringProcess.class)) {
            for (GenericHistory hist : HistoryManager.getInstance().getHistoryForType(cls.getSimpleName())) {
                total.requests += hist.getAllRequestInfo().getNumberOfRequests();
                total.requestTime += hist.getAllRequestInfo().getTotalRequestTimeMS();
            }
        }

        total.format((System.currentTimeMillis() - servletStartTime) / 1000);

        RecentData rd = HistoryManager.getInstance().getRecentData();
        if (rd != null) {
            recent.requestTime = rd.getRequests();
            recent.requests = rd.getRequests();
            recent.format((rd.getEndTime() - rd.getStartTime()) / 1000);
        } else {
            recent.requestTime = 0;
            recent.requests = 0;
            recent.format(0);
        }

        ObjectTriplet<Boolean, String, String> ret = RuntimeUtils.exec("uptime", new RuntimeUtils.Options(true));
        if (ret.getOne()) {
            String search = "load average";
            int pos = ret.getTwo() != null ? ret.getTwo().indexOf(search) : -1;
            if (pos > -1) {
                if (ret.getTwo().charAt(search.length() + pos) == ':') {
                    unixUptime = ret.getTwo().substring(pos + search.length() + 1).trim();
                } else {
                    unixUptime = ret.getTwo().substring(pos + search.length() + 3);
                }
            }
        }

        return null;

    }

    public long getUptimeMinutes() {
        return uptimeMinutes;
    }

    public Stats getTotal() {
        return total;
    }

    public Stats getRecent() {
        return recent;
    }

    public String getUnixUptime() {
        return unixUptime;
    }

    public long getLastHeartbeatServerPing() {
        return HeartbeatServer.INSTANCE.getLastPing();
    }

    public long getServletStartTime() {
        return servletStartTime;
    }

    public boolean isHeartbeatServerRunning() {
        return HeartbeatServer.INSTANCE.isRunning();
    }
}
