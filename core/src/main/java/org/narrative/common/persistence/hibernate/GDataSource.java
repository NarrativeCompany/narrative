package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.OID;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import java.sql.SQLException;

/**
 * Date: Feb 1, 2008
 * Time: 10:22:18 AM
 *
 * @author brian
 */
public class GDataSource {
    private final ComboPooledDataSource comboPooledDataSource;
    private final OID partitionOid;

    public GDataSource(ComboPooledDataSource comboPooledDataSource, OID partitionOid) {
        this.comboPooledDataSource = comboPooledDataSource;
        this.partitionOid = partitionOid;
    }

    public GConnection getConnection() throws SQLException {
        return new GConnection(comboPooledDataSource, partitionOid);
    }

    public void destroy() throws SQLException {
        DataSources.destroy(comboPooledDataSource);
    }

    public String getDataSourceName() {
        return comboPooledDataSource.getDataSourceName();
    }

    public String getIdentityToken() {
        return comboPooledDataSource.getIdentityToken();
    }

    public int getNumIdleConnectionsDefaultUser() throws SQLException {
        return comboPooledDataSource.getNumIdleConnectionsDefaultUser();
    }

    public int getNumBusyConnectionsDefaultUser() throws SQLException {
        return comboPooledDataSource.getNumBusyConnectionsDefaultUser();
    }

    public int getNumConnectionsDefaultUser() throws SQLException {
        return comboPooledDataSource.getNumConnectionsDefaultUser();
    }

    public OID getPartitionOid() {
        return partitionOid;
    }

    public ComboPooledDataSource getComboPooledDataSource() {
        return comboPooledDataSource;
    }
}
