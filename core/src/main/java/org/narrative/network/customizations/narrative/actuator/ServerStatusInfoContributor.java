package org.narrative.network.customizations.narrative.actuator;

import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.cluster.actions.ServerStatusTask;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.system.NetworkVersion;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/18/18
 * Time: 7:55 PM
 *
 * @author brian
 */
@Component
public class ServerStatusInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        ServerStatusTask serverStatusTask = new ServerStatusTask();
        networkContext().doGlobalTask(serverStatusTask);

        builder.withDetail("clusterId", NetworkRegistry.getInstance().getClusterId())
                .withDetail("version", NetworkVersion.INSTANCE.getVersion())
                .withDetail("jenkinsBuild", NetworkVersion.INSTANCE.getJenkinsBuild())
                .withDetail("branch", NetworkVersion.INSTANCE.getBranch())
                .withDetail("gitSha", NetworkVersion.INSTANCE.getGitSha())
                .withDetail("servletName", NetworkRegistry.getInstance().getServletName())
                .withDetail("buildDate", Instant.ofEpochMilli(NetworkRegistry.getInstance().getGlobalLastModifiedTime()))

                .withDetail("serverStartTime", Instant.ofEpochMilli(serverStatusTask.getServletStartTime()))
                .withDetail("uptimeMinutes", serverStatusTask.getUptimeMinutes())
                .withDetail("uptime", IPStringUtil.getTrimmedString(serverStatusTask.getUnixUptime()));

        ServerStatusTask.Stats totalStats = serverStatusTask.getTotal();

        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("avgResponseTime", totalStats.getAvgResponseTime());
        stats.put("avgRequestPerSecond", totalStats.getAvgResponseTime());
        stats.put("totalRequests", totalStats.getRequests());
        stats.put("totalRequestTime", totalStats.getRequestTime());
        builder.withDetail("stats", stats);
    }
}
