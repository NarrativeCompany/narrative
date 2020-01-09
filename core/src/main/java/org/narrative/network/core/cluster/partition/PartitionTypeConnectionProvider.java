package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Stoppable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Date: Nov 4, 2009
 * Time: 10:35:33 AM
 *
 * @author brian
 */
public class PartitionTypeConnectionProvider implements ConnectionProvider, Configurable, Stoppable {

    private PartitionType partitionType;

    private static final ThreadLocal<OID> CURRENT_PARTITION_OID = new ThreadLocal<>();
    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    public static void setCurrentPartitionOid(OID partitionOid) {
        if (partitionOid == null) {
            CURRENT_PARTITION_OID.remove();
        } else {
            CURRENT_PARTITION_OID.set(partitionOid);
        }
    }

    public static void setCurrentConnection(Connection connection) {
        if (connection == null) {
            CURRENT_CONNECTION.remove();
        } else {
            CURRENT_CONNECTION.set(connection);
        }
    }

    /**
     * Configure the service.
     *
     * @param configurationValues The configuration properties.
     */
    @Override
    public void configure(Map configurationValues) {
        Properties props = new Properties();
        props.putAll(configurationValues);
        String partitionTypeStr = props.getProperty(PartitionType.PARTITION_TYPE_PROPERTY, null);
        if (!IPStringUtil.isEmpty(partitionTypeStr)) {
            partitionType = PartitionType.valueOf(partitionTypeStr);
        }
        if (partitionType == null) {
            throw UnexpectedError.getRuntimeException("Failed initializing PartitionType for PartitionTypeConnectionProvider!", true);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        assert CURRENT_PARTITION_OID.get() != null : "Should always have a current partition OID when getting a connection via Hibernate.  Why is this not happening? pt/" + partitionType;
        Connection con = CURRENT_CONNECTION.get();
        if(con!=null) {
            return con;
        }
        PartitionConnectionPool partitionConnectionPool = PartitionConnectionPool.getPartitionConnectionPool(CURRENT_PARTITION_OID.get());
        con = partitionConnectionPool.getDataSource().getConnection();
        // we have enabled hibernate.connection.provider_disables_autocommit, so Hibernate's auto-commit is disabled
        // by default. in doing so, we want to ensure that all connections we return to Hibernate already have
        // auto-commit disabled (which they warn about in a debug message at init time when using this setting)
        con.setAutoCommit(false);
        return con;
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        PersistenceUtil.closeConnection(conn);
    }

    /**
     * Stop phase notification
     */
    @Override
    public void stop() {
        // bl: if the partition OIDs haven't yet been initialized, then there isn't really anything to close.
        Set<OID> partitionOids = PartitionConnectionPool.partitionTypeToPartitionOids.get(partitionType);
        if (partitionOids == null) {
            return;
        }
        for (OID partitionOid : partitionOids) {
            try {
                PartitionConnectionPool.getPartitionConnectionPool(partitionOid).getDataSource().destroy();
            } catch (SQLException e) {
                throw UnexpectedError.getRuntimeException("Failed destroying datasource at shutdown!", e);
            }
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    public boolean isUnwrappableAs(Class unwrapType) {
        return ConnectionProvider.class.equals(unwrapType) || PartitionTypeConnectionProvider.class.isAssignableFrom(unwrapType);
    }

    public <T> T unwrap(Class<T> unwrapType) {
        if (!ConnectionProvider.class.equals(unwrapType) && !PartitionTypeConnectionProvider.class.isAssignableFrom(unwrapType)) {
            throw new UnknownUnwrapTypeException(unwrapType);
        }
        //noinspection unchecked
        return (T) this;
    }
}
