package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.common.util.processes.SQLProcess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 1:32:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseResources {

    private static final NarrativeLogger logger = new NarrativeLogger(DatabaseResources.class);

    private GConnection con;
    private final GDataSource dataSource;
    private final Map<String, String> namedSqlStatements;

    public DatabaseResources(GDataSource dataSource, Map<String, String> namedSqlStatements) {
        this.dataSource = dataSource;
        this.namedSqlStatements = namedSqlStatements;
    }

    public GDataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        if (con == null) {
            try {
                con = dataSource.getConnection();
            } catch (SQLException sqle) {
                Debug.assertMsg(logger, false, "Failed getting connection", sqle);
            }
        }
        return con;
    }

    public void executeNamedStatementWithReplace(String namedStatement, String... replacements) throws SQLException {
        executeNamedStatementWithReplace(namedStatement, Collections.emptyList(), replacements);
    }

    public <T> void executeNamedStatementWithReplace(String namedStatement, List<T> params, String... replacements) throws SQLException {
        String sql = getNamedSql(namedStatement);
        int pos = 0;
        for (String replacement : replacements) {
            sql = sql.replaceAll("\\{" + pos++ + "\\}", replacement);
        }
        executeStatement(sql, params.toArray());
    }

    public int executeStatementSafely(String stmtString, Object... parameters) {
        return executeStatementSafely(stmtString, parameters == null ? null : Arrays.asList(parameters));
    }

    public int executeStatementSafely(String stmtString, List<Object> parameters) {
        return executeStatementSafely(stmtString, null, parameters);
    }

    public int executeStatementSafely(String stmtString, String sqlProcessName, List<Object> parameters) {
        try {
            return executeStatement(stmtString, sqlProcessName, parameters);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Failed executing SQL statement! sql/" + stmtString, e);
        }
    }

    public int executeNamedStatement(String name) throws SQLException {
        return executeStatement(getNamedSql(name), name);
    }

    public int executeNamedStatementSafely(String name) {
        return executeStatementSafely(getNamedSql(name), name, null);
    }

    public int executeNamedStatement(String name, Object... parameters) throws SQLException {
        return executeStatement(getNamedSql(name), name, Arrays.asList(parameters));
    }

    public int executeNamedStatement(String name, List<Object> parameters) throws SQLException {
        return executeStatement(getNamedSql(name), name, parameters);
    }

    public int executeStatement(String stmtString, Object... parameters) throws SQLException {
        return executeStatement(stmtString, null, parameters);
    }

    public int executeStatement(String stmtString, String sqlProcessName, Object... parameters) throws SQLException {
        return executeStatement(stmtString, sqlProcessName, parameters == null ? null : Arrays.asList(parameters));
    }

    /**
     * execute a SQL statement.  shouldn't be a query.  should only be used
     * with statements that are not preparable (i.e. drop table, create database, etc.).
     *
     * @param stmtString the SQL statement to execute
     * @return the row update count
     * @throws SQLException
     */
    private int executeStatement(final String stmtString, String sqlProcessName, final List<Object> parameters) throws SQLException {
        if (isEmpty(sqlProcessName)) {
            sqlProcessName = getSQLProcessNameFromSQL(stmtString);
        }
        final String fSqlProcessName = sqlProcessName;
        return doSQLTask(new SQLTask<Integer>() {
            public Integer doSQLTask(Connection connection) throws SQLException {
                long startTimeMs = System.currentTimeMillis();
                PreparedStatement stmt = connection.prepareStatement(stmtString);
                try {
                    if (parameters != null && !parameters.isEmpty()) {
                        for (int i = 0; i < parameters.size(); i++) {
                            Object parameter = parameters.get(i);
                            setParameterOnPreparedStatement(stmt, i + 1, parameter);
                        }
                    }
                    stmt.execute();
                    int ret = stmt.getUpdateCount();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Finished executing " + fSqlProcessName + " in " + dataSource.getDataSourceName() + " with updateCount of " + ret + " in " + (System.currentTimeMillis() - startTimeMs) + "ms");
                    }
                    return ret;
                } finally {
                    stmt.close();
                }
            }
        }, sqlProcessName);
    }

    public void setParametersOnPreparedStatement(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters != null && !parameters.isEmpty()) {
            for (int i = 0; i < parameters.size(); i++) {
                Object parameter = parameters.get(i);
                setParameterOnPreparedStatement(stmt, i + 1, parameter);
            }
        }
    }

    public int executeBatchPreparedStatement(final PreparedStatement stmt, final String sqlProcessName) throws SQLException {
        return doSQLTask(new SQLTask<Integer>() {
            public Integer doSQLTask(Connection connection) throws SQLException {
                long startTimeMs = System.currentTimeMillis();
                try {
                    stmt.executeBatch();
                    int ret = stmt.getUpdateCount();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Finished executing batch " + sqlProcessName + " in " + dataSource.getDataSourceName() + " with updateCount of " + ret + " in " + (System.currentTimeMillis() - startTimeMs) + "ms");
                    }
                    return ret;
                } finally {
                    stmt.close();
                }
            }
        }, sqlProcessName);
    }

    public Object executeScalarNamedStatement(String name) throws SQLException {
        return executeScalarStatement(getNamedSql(name), name, Collections.emptyList());
    }

    public Object executeScalarNamedStatement(String name, List<?> parameters) throws SQLException {
        return executeScalarStatement(getNamedSql(name), name, parameters);
    }

    public Object executeScalarStatement(String stmtString) throws SQLException {
        return executeScalarStatement(stmtString, null, null);
    }

    public Object executeScalarStatement(String stmtString, List<?> parameters) throws SQLException {
        return executeScalarStatement(stmtString, null, parameters);
    }

    private Object executeScalarStatement(final String stmtString, String sqlProcessName, final List<?> parameters) throws SQLException {
        if (isEmpty(sqlProcessName)) {
            sqlProcessName = getSQLProcessNameFromSQL(stmtString);
        }
        return doSQLTask(new SQLTask<Object>() {
            public Object doSQLTask(Connection connection) throws SQLException {
                PreparedStatement stmt = connection.prepareStatement(stmtString);
                try {
                    if (parameters != null) {
                        for (int i = 0; i < parameters.size(); i++) {
                            stmt.setObject(i + 1, parameters.get(i));
                        }
                    }
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        return null;
                    }

                    return rs.getObject(1);
                } finally {
                    stmt.close();
                }
            }
        }, sqlProcessName);
    }

    /**
     * execute multiple named statements based on a base query name in a single
     * sql transaction.  for a base query name of:
     * query_to_do, this will execute queries named as:
     * query_to_do1
     * query_to_do2
     * ...
     * until it doesn't find the query in the DatabaseResources.
     *
     * @param baseQueryName the base query name to execute multiple statements for
     */
    public void executeMultiNamedStatements(String baseQueryName) throws MultiStatementQueryFailure {
        executeMultiNamedStatements(baseQueryName, true, 1, null);
    }

    public void executeMultiNamedStatements(String baseQueryName, Map<String, String> sqlReplacements) throws MultiStatementQueryFailure {
        executeMultiNamedStatements(baseQueryName, true, 1, sqlReplacements);
    }

    public void executeMultiNamedStatements(final String baseQueryName, final boolean useTransaction, final int startStatement, Map<String, String> sqlReplacements) throws MultiStatementQueryFailure {
        Connection connection = getConnection();
        try {
            boolean autoCommitOriginal = connection.getAutoCommit();
            String sql = null;
            int i = 1;
            try {
                // execute all of the SQL statements as a single isolated transaction
                connection.setAutoCommit(!useTransaction);
                for (i = startStatement; i <= Integer.MAX_VALUE; i++) {
                    final String queryName = baseQueryName + i;
                    try {
                        sql = getNamedSql(queryName);

                        if (sqlReplacements != null) {
                            for (Map.Entry<String, String> replacementEntry : sqlReplacements.entrySet()) {
                                sql = sql.replaceAll(replacementEntry.getKey(), replacementEntry.getValue());
                            }
                        }
                    } catch (UnexpectedError t) {
                        if (i == 1) {
                            //should have at least one query
                            throw t;
                        } else {
                            // no more queries found
                            break;
                        }
                    }

                    final String sqlString = sql;

                    doSQLTask(connection, new SQLTask<Object>() {
                        public Object doSQLTask(Connection connection) throws SQLException {
                            long startTimeMs = System.currentTimeMillis();
                            Statement stmt = connection.createStatement();
                            int count = stmt.executeUpdate(sqlString);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Finished executing " + queryName + " in " + dataSource.getDataSourceName() + " with updateCount of " + count + " in " + (System.currentTimeMillis() - startTimeMs) + "ms" + " - " + (sqlString.replaceAll("\\r\\n", " ").replaceAll("\\n", " ").replaceAll("\\r", " ")));
                            }
                            return null;
                        }
                    }, queryName);
                }
                // once we're done executing all of the statements, commit them to the database.
                if (useTransaction) {
                    connection.commit();
                }

            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed executing " + baseQueryName + i + " in " + dataSource.getDataSourceName() + ": " + sql, e);
                }
                try {
                    if (useTransaction) {
                        connection.rollback();
                    }
                } catch (SQLException sqle) {
                    logger.error("Failed rolling back transaction!", sqle);
                }
                throw new MultiStatementQueryFailure("Failed executing multi-sql on " + baseQueryName + i + ": " + sql, i, e);
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException sqle) {
            throw UnexpectedError.getRuntimeException("Failed getting/setting transaction autoCommit for " + baseQueryName, sqle);
        }
    }

    public List<OID> getOIDsFromSQL(String sql) {
        List<OID> ret = new LinkedList<OID>();
        try {
            ResultSet rs = null;
            try {
                rs = getResultSet(sql);
                while (rs.next()) {
                    ret.add(OID.valueOf(rs.getLong(1)));
                }
            } finally {
                PersistenceUtil.close(rs);
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Failed getting OIDs from SQL for sql/" + sql, e);
        }
        return ret;
    }

    public void processResultSetFromNamedSQL(String namedStatement, ResultSetSQLHandler handler) {
        processResultSetFromSQL(getNamedSql(namedStatement), namedStatement, handler, false, null);
    }

    public void processResultSetFromSQL(String sql, ResultSetSQLHandler handler) {
        processResultSetFromSQL(sql, null, handler);
    }

    public void processResultSetFromSQL(String sql, String sqlProcessName, ResultSetSQLHandler handler) {
        processResultSetFromSQL(sql, sqlProcessName, handler, false, null);
    }

    public void processResultSetFromSQLWithArgs(String sql, Collection<? extends Object> args, ResultSetSQLHandler handler) {
        assert args != null && !args.isEmpty() : "Must supply args for replacement!";
        processResultSetFromSQLWithArgs(sql, null, args, handler);
    }

    public void processResultSetFromSQLWithArgs(String sql, String sqlProcessName, Collection<? extends Object> args, ResultSetSQLHandler handler) {
        assert args != null && !args.isEmpty() : "Must supply args for replacement!";
        processResultSetFromSQL(sql, sqlProcessName, handler, false, args);
    }

    public void processResultSetFromSQLForUpdate(String sql, ResultSetSQLHandler handler) {
        processResultSetFromSQL(sql, null, handler, true, null);
    }

    private void processResultSetFromSQL(String sql, String sqlProcessName, ResultSetSQLHandler handler, boolean forUpdate, Collection<? extends Object> args) {
        ResultSet rs = null;
        try {
            rs = getResultSet(sql, sqlProcessName, forUpdate, args);
            while (rs.next()) {
                handler.handleResultSetRow(rs);
                if (forUpdate) {
                    rs.updateRow();
                }
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Failed processing ResultSet from SQL! sql/" + sql, e);
        } finally {
            PersistenceUtil.close(rs);
        }
    }

    public ResultSet getResultSetFromNamedSql(String name, Object... args) throws SQLException {
        return getResultSet(getNamedSql(name), name, args);
    }

    public ResultSet getResultSet(final String sql, final Object... args) throws SQLException {
        return getResultSet(sql, null, args);
    }

    private ResultSet getResultSet(final String sql, String sqlProcessName, final Object... args) throws SQLException {
        return getResultSet(sql, sqlProcessName, false, Arrays.asList(args));
    }

    private ResultSet getResultSet(final String sql, String sqlProcessName, final boolean forUpdate, final Collection<? extends Object> args) throws SQLException {
        if (isEmpty(sqlProcessName)) {
            sqlProcessName = getSQLProcessNameFromSQL(sql);
        }
        return doSQLTask(new SQLTask<ResultSet>() {
            public ResultSet doSQLTask(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = null;
                if (forUpdate) {
                    preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                } else {
                    preparedStatement = connection.prepareStatement(sql);
                }
                if (args != null && !args.isEmpty()) {
                    int i = 1;
                    for (Object arg : args) {
                        setParameterOnPreparedStatement(preparedStatement, i, arg);
                        i++;
                    }
                }

                return preparedStatement.executeQuery();
            }
        }, sqlProcessName);
    }

    private static void setParameterOnPreparedStatement(PreparedStatement stmt, int parameterIndex, Object parameter) throws SQLException {
        // any other custom types we need to support here?
        if (parameter == null) {
            stmt.setNull(parameterIndex, Types.NULL);
        } else if (parameter instanceof OID) {
            stmt.setLong(parameterIndex, ((OID) parameter).getValue());
        } else if (parameter.getClass().isArray() && parameter.getClass().getComponentType() == Byte.TYPE) {
            stmt.setBytes(parameterIndex, (byte[]) parameter);
        } else {
            stmt.setObject(parameterIndex, parameter);
        }
    }

    public String getNamedSql(String name) {
        if (namedSqlStatements == null) {
            throw UnexpectedError.getRuntimeException("Can't get named SQL for resources that didn't provide named SQL at startup!", true);
        }
        String sql = namedSqlStatements.get(name);
        if (isEmpty(sql)) {
            throw UnexpectedError.getRuntimeException("Cannot find named sql statement: " + name);
        }

        return sql;
    }

    private String getSQLProcessNameFromSQL(String sql) {
        // todo: we may want to use something other than the full SQL string for the process name
        return dataSource.getDataSourceName() + ": " + sql;
    }

    /**
     * free any resources associated with this DatabaseResources instance.
     * releases any open connection back to the connection pool.
     */
    public void freeResources() {
        freeConnection();
    }

    protected void freeConnection() {
        if (con != null) {
            PersistenceUtil.closeConnection(con);
            con = null;
        }
    }

    public <T> T doSQLTask(SQLTask<T> sqlTask, String sqlProcessName) throws SQLException {
        return doSQLTask(getConnection(), sqlTask, sqlProcessName);
    }

    public <T> T doSQLTask(Connection connection, SQLTask<T> sqlTask, String sqlProcessName) throws SQLException {
        SQLProcess process = new SQLProcess(sqlProcessName);
        ProcessManager.getInstance().pushProcess(process);
        try {
            return sqlTask.doSQLTask(connection);
        } finally {
            ProcessManager.getInstance().popProcess();
        }
    }

    public boolean isTablePresent(String databaseName, String tableName) {
        ResultSet rs = null;
        try {
            rs = getResultSet("select count(*) from information_schema.TABLES t " + "where t.table_schema = ? and t.table_name = ?", null, databaseName, tableName);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for table: " + tableName, e);
        } finally {
            PersistenceUtil.close(rs);
        }
        return false;
    }

    /**
     * create a federated table in this database pointing to a separate database as specified by the supplied parameters
     * note: create federated table queries should be of the following format:
     * <p>
     * create table TableName (
     * ...table definition...
     * ) ENGINE=FEDERATED CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'
     * CONNECTION='mysql://{0}:{1}@{2}/{3}/TableName';
     *
     * @param federatedTableCreateQueryName the query key in the sql file of the create table sql query
     * @param username                      the username of the federated database
     * @param password                      the password of the federated database
     * @param server                        the server the federated database lives on
     * @param databaseName                  the name of the federated database
     */
    public void createFederatedTable(String federatedTableCreateQueryName, String username, String password, String server, String databaseName) {
        String sql = getNamedSql(federatedTableCreateQueryName);
        sql = sql.replaceAll("\\{0\\}", username);
        sql = sql.replaceAll("\\{1\\}", password);
        sql = sql.replaceAll("\\{2\\}", server);
        sql = sql.replaceAll("\\{3\\}", databaseName);
        executeStatementSafely(sql, federatedTableCreateQueryName, null);
    }

    private interface SQLTask<T> {
        public T doSQLTask(Connection connection) throws SQLException;
    }

    public interface ResultSetSQLHandler {
        public void handleResultSetRow(ResultSet rs) throws SQLException;
    }
}
//private Set<ResultSet> resultSetsInUse = new HashSet<ResultSet>();
//private Map<String, PreparedStatement> preparedStatements = new HashMap<String,PreparedStatement>();
/**
 * Given a name and stmt string, return a stmt.  If name has been used before
 * return the existing PreparedStatement, saving a trip to the deatabase
 * <p>
 * gets the prepared statement with a flag whether to output to debug
 * trace (yes, we were getting infinite recursion on the database logger)
 * <p>
 * Given a name and stmt string, return a callable stmt.  If name has been
 * used before, return the existing PreparedStatement.
 * Saves a trip to the database.
 * <p>
 * BTW: callable statements start like this:
 * "{? = call func (?,?)}"
 */
    /*public PreparedStatement getPreparedStatement(String stmtString) {
        return getPreparedStatement(stmtString, true);
    }

    public PreparedStatement getNamedPreparedStatement(String name) {
        return getPreparedStatement(getNamedSql(name));
    }

    public PreparedStatement getNamedPreparedStatement(String name, boolean isDebugTracing) {
        return getPreparedStatement(getNamedSql(name), isDebugTracing);
    }*/

/**
 * gets the prepared statement with a flag whether to output to debug
 * trace (yes, we were getting infinite recursion on the database logger)
 */
    /*public PreparedStatement getPreparedStatement(String stmtString, boolean isDebugTracing) {
        Connection connection = getConnection();
        String psKey = new StringBuffer(stmtString)
                .append(DBSetManager.getInstance().getCurrentDBSetName())
                .append("PS").toString();
        PreparedStatement stmt = preparedStatements.get(psKey);
        if(stmt!=null) {
            if(isDebugTracing) if(logger.isDebugEnabled()) logger.debug( "JDBC: Returning existing prepared statement: " + IPStringUtil.getTruncatedString(stmtString, 80) + "...");
            // tidy things up from last time
            try {
                stmt.clearParameters();
                stmt.clearWarnings();
                stmt.setQueryTimeout(0);
            } catch(SQLException ignore) {
                if(isDebugTracing) logger.error( "could not clear parameters: " + ignore.toString());
            }
            return stmt;
        }

        if(logger.isDebugEnabled()) logger.debug("JDBC: Preparing statement:\n" + stmtString);
        try {
            stmt = connection.prepareStatement(stmtString);
        } catch(SQLException e) {
            throw UnexpectedError.getRuntimeException("could not prepare statement: ", e);
        }
        // bl: no longer caching prepared statements
        //PreparedStatementCache.setCachedRequest(connection, stmtString, stmt);
        // bl: tracking prepared statements locally so we can clean them up when freeing resources
        preparedStatements.put(psKey, stmt);
        return stmt;
    }*/
/**
 * Given a name and stmt string, return a callable stmt.  If name has been
 * used before, return the existing PreparedStatement.
 * Saves a trip to the database.
 *
 * BTW: callable statements start like this:
 * "{? = call func (?,?)}"
 */
    /*public GCallableStatement getCallableStatement(String stmtString) {
        Connection connection = getConnection();
        String psKey = new StringBuffer(stmtString)
                .append(DBSetManager.getInstance().getCurrentDBSetName())
                .append("CS").toString();
        GCallableStatement stmt = (GCallableStatement) preparedStatements.get(psKey.toString());

        if(stmt!=null) {
            if(logger.isDebugEnabled()) logger.debug( "JDBC: Returning existing call: " + IPStringUtil.getTruncatedString(stmtString, 80) + "...");
            return stmt;
        }

        if(logger.isDebugEnabled()) logger.debug("JDBC: Preparing callable stmt:\n" + stmtString);
        try {
            // dynamically replace the db names in the SQL
            stmt = connection.prepareCall(stmtString);
        } catch(SQLException e) {
            if(Debug.bDebug) Debug.assertMsg(logger, false, "could not rollback transaction: " + e.toString());
        }
        // bl: no longer caching prepared statements
        //PreparedStatementCache.setCachedRequest(connection, stmtString, stmt);
        // bl: tracking prepared statements locally so we can clean them up when freeing resources
        preparedStatements.put(psKey, stmt);
        return stmt;
    }
    public GCallableStatement getNamedCallableStatement(String name) {
        return getCallableStatement(getNamedSql(name));
    }*/

    /*public ResultSet registerResultSet(ResultSet rs) {
        if(rs==null)
            return null;
        resultSetsInUse.add(rs);
        return rs;
    }
    public void deregisterResultSet(ResultSet rs) {
        if(rs==null)
            return;
        try {
            if(!resultSetsInUse.remove(rs)) {
                logger.error( "Freeing a result set that is not registered on this cache.  Ignoring the problem, but you might want to take a look.", new Throwable("A stack dump for you"));
            }
            rs.close();
        } catch(SQLException sqle) {
            logger.warn( "Failed closing result set, oh well.  Carry on.", sqle);
        }
    }*/

    /*public void freeResources() {
        freeResultSets();
        freePreparedStatements();
        freeConnection();
    }*/
    /*protected void freeResultSets() {
        Iterator iter = resultSetsInUse.iterator();
        while(iter.hasNext()) {
            try {
                ResultSet rs = (ResultSet)iter.next();
                // get some debug info about the result set
                StringBuffer metaData = new StringBuffer();
                {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int cols = rsmd==null?0:rsmd.getColumnCount();
                    for(int i=1; i<=cols; i++) {
                        if(i>1)
                            metaData.append(", ");
                        metaData.append(rsmd.getCatalogName(i) + "." + rsmd.getTableName(i) + "." + rsmd.getColumnName(i));
                    }
                }
                rs.close();
                logger.warn( "Closing result sets.  Why wasn't this done before the endoftask cleanup? you may want to look into this. rsmd: " + metaData, new Throwable());
            } catch(SQLException sqle) {
                if(logger.isInfoEnabled()) logger.info( "Ignoring problem closing result set", sqle);
            }
        }
        resultSetsInUse.clear();
    }*/
    /*protected void freePreparedStatements() {
        // bl: no longer caching prepared statements
        //PreparedStatementCache.cleanupConnection(con);
        // bl: close all prepared statements that may not have been closed
        Iterator iter = preparedStatements.values().iterator();
        while(iter.hasNext()) {
            try {
                PreparedStatement stmt = (PreparedStatement)iter.next();
                // even if the prepared statement has already been closed, it won't
                // hurt anything to try to close it again.
                stmt.close();
            } catch(SQLException sqle) {
                logger.error( "Ignoring problem cleaning up prepared statements", sqle);
            }
        }
        preparedStatements.clear();
    }*/
//}
