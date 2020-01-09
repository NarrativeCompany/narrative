package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2005
 * Time: 7:51:39 PM
 */
public class GConnection implements Connection {

    private Boolean readOnly;
    private Integer transactionIsolationLevel;
    private Boolean isAutoCommit;
    private Boolean isClosed;
    private Connection connection;
    private final ComboPooledDataSource comboPooledDataSource;
    private final OID partitionOid;

    GConnection(ComboPooledDataSource comboPooledDataSource, OID partitionOid) {
        this.comboPooledDataSource = comboPooledDataSource;
        this.partitionOid = partitionOid;
    }

    public OID getPartitionOid() {
        return partitionOid;
    }

    public void initConnection() {
        // bl: this will make sure that we have obtained a Connection from the underlying connection pool.
        getConnectionWrapped();
    }

    private synchronized Connection getConnection() throws SQLException {

        if (connection == null) {
            connection = comboPooledDataSource.getConnection();
            // bl: if we had set the readOnly flag, then update the connection right after creating it.
            if (readOnly != null) {
                connection.setReadOnly(readOnly);
            }
            if (transactionIsolationLevel != null) {
                connection.setTransactionIsolation(transactionIsolationLevel);
            }
            if (isAutoCommit != null) {
                connection.setAutoCommit(isAutoCommit);
            }
        }

        return connection;
    }

    public ComboPooledDataSource getComboPooledDataSource() {
        return comboPooledDataSource;
    }

    public int getHoldability() throws SQLException {
        return getConnection().getHoldability();
    }

    public void setHoldability(int holdability) throws SQLException {
        getConnection().setHoldability(holdability);
    }

    public int getTransactionIsolation() throws SQLException {
        return getConnection().getTransactionIsolation();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        transactionIsolationLevel = level;
        if (connection != null) {
            getConnection().setTransactionIsolation(level);
        }
    }

    public void clearWarnings() throws SQLException {
        // bl: if we haven't even gotten a connection yet, then there aren't any warnings to clear :)
        if (connection == null) {
            return;
        }
        getConnection().clearWarnings();
    }

    public void close() throws SQLException {
        // bl: no need to instantiate a connection just to subsequently close it
        if (connection != null) {
            try {
                getConnection().close();
            } finally {
                connection = null;
            }
        } else {
            // bl: track the fact that this connection was closed so that we can return
            // the proper value in isClosed().
            isClosed = true;
        }
    }

    public void commit() throws SQLException {
        if (connection != null) {
            getConnection().commit();
        }
    }

    public void rollback() throws SQLException {
        if (connection != null) {
            getConnection().rollback();
        }
    }

    public boolean getAutoCommit() throws SQLException {
        // bl: when establishing a transaction initially in Hibernate, Hibernate will check the current auto-commit
        // status so that it can reset the auto-commit value at the end of the transaction.
        // for MySQL, auto-commit is always on by default from what I can tell, so changing the behavior
        // here to automatically set auto-commit to true during the initial checks.  this saves us in some
        // circumstances from ever checking a connection out from the database.
        if (connection == null) {
            return isAutoCommit != null ? isAutoCommit : true;
        }
        return getConnection().getAutoCommit();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        isAutoCommit = autoCommit;
        if (connection != null) {
            getConnection().setAutoCommit(autoCommit);
        }
    }

    public boolean isClosed() throws SQLException {
        // bl: assume the connection is not closed if we haven't even established a connection yet :)
        if (connection == null) {
            // if the isClosed flag has not been set, then the connection is not closed.
            return isClosed != null ? isClosed : false;
        }
        return getConnection().isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        // bl: only can use the readOnly flag if it was set previously.
        if (readOnly != null) {
            if (connection != null) {
                assert readOnly == getConnection().isReadOnly() : "The internal readOnly flag should always match the flag on the connection!";
            }
            return readOnly;
        }
        // if the readOnly flag isn't set, we need to get the readOnly flag from the Connection
        return getConnection().isReadOnly();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        if (connection != null) {
            getConnection().setReadOnly(readOnly);
        }
    }

    public String getCatalog() throws SQLException {
        return getConnection().getCatalog();
    }

    public void setCatalog(String catalog) throws SQLException {
        getConnection().setCatalog(catalog);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }

    public SQLWarning getWarnings() throws SQLException {
        if (connection == null) {
            return null;
        }
        return getConnection().getWarnings();
    }

    public Savepoint setSavepoint() throws SQLException {
        return getConnection().setSavepoint();
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        getConnection().releaseSavepoint(savepoint);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        getConnection().rollback(savepoint);
    }

    public Statement createStatement() throws SQLException {
        return new GStatement(getConnection().createStatement());
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new GStatement(getConnection().createStatement(resultSetType, resultSetConcurrency));
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new GStatement(getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return getConnection().getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        getConnection().setTypeMap(map);
    }

    public String nativeSQL(String sql) throws SQLException {
        return getConnection().nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return new GCallableStatement(sql, getConnection().prepareCall(sql));
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new GCallableStatement(sql, getConnection().prepareCall(sql, resultSetType, resultSetConcurrency));
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new GCallableStatement(sql, getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql));
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql, autoGeneratedKeys));
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency));
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql, columnIndexes));
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return getConnection().setSavepoint(name);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new GPreparedStatement(sql, getConnection().prepareStatement(sql, columnNames));
    }

    public Clob createClob() throws SQLException {
        return getConnection().createClob();
    }

    public Blob createBlob() throws SQLException {
        return getConnection().createBlob();
    }

    public NClob createNClob() throws SQLException {
        return getConnection().createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return getConnection().createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return getConnection().isValid(timeout);
    }

    private Connection getConnectionWrapped() {
        try {
            return getConnection();
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Error getting connection", e);
        }
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        getConnectionWrapped().setClientInfo(name, value);
    }

    public String getClientInfo(String name) throws SQLException {
        return getConnection().getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return getConnection().getClientInfo();
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        getConnectionWrapped().setClientInfo(properties);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return getConnection().createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return getConnection().createStruct(typeName, attributes);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getConnection().unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getConnection().isWrapperFor(iface);
    }

    @Override
    public String getSchema() throws SQLException {
        return getConnection().getSchema();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        getConnection().setSchema(schema);
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        getConnection().abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        getConnection().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return getConnection().getNetworkTimeout();
    }
}
