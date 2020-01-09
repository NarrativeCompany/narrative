package org.narrative.network.core.quartz.services;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.utils.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: barry
 * Date: Mar 3, 2010
 * Time: 10:46:45 AM
 */
public class QuartzConnectionProvider implements ConnectionProvider {
    private static final NetworkLogger logger = new NetworkLogger(QuartzConnectionProvider.class);

    @Override
    public void initialize() throws SQLException {
        // nothing to initialize
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting connection for QuartzScheduler");
        }
        return PartitionType.GLOBAL.currentSession().getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        //nothing to be done
    }
}
