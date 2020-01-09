package org.narrative.network.core.cluster.actions.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 16, 2006
 * Time: 4:32:57 PM
 */
public class LoggingInfoAction extends SystemMonitoringAction {

    Collection<Logger> allLoggers;

    @Override
    public String input() throws Exception {
        return super.input();
    }

    public LoggingMXBean getLoggingBean() {
        return LogManager.getLoggingMXBean();
    }

    public Collection<Logger> getAllLoggers() {
        allLoggers = new HashSet<Logger>();
        LogManager.getLoggingMXBean().getLoggerNames();
        for (String loggerName : LogManager.getLoggingMXBean().getLoggerNames()) {
            Logger logger = LogManager.getLogManager().getLogger(loggerName);
            allLoggers.add(logger.getParent());

        }
        return allLoggers;
    }
}
