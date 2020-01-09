package org.narrative.network.core.cluster.partition;

import org.narrative.common.core.services.NarrativeAnnotationConfiguration;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.persistence.hibernate.GConnection;
import org.narrative.common.persistence.hibernate.GDataSource;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.RuntimeUtils;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.dao.PartitionDAO;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.versioning.services.PatchRunner;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.Dialect;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:38:28 PM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "DBPartition")
public class Partition implements DAOObject<PartitionDAO> {

    private static final NetworkLogger logger = new NetworkLogger(Partition.class);

    public static final String FIELD__DATABASE_NAME__NAME = "databaseName";

    private OID oid;
    private String server;
    private String databaseName;
    private String username;
    private String password;
    private String parameters;
    private String rootPassword;
    private int weight = 1;
    private PartitionType partitionType;

    private transient PartitionKey partitionKey;

    public Partition(PartitionType partitionType) {
        this.partitionType = partitionType;
    }

    /**
     * @deprecated for hibernate use only
     */
    public Partition() {
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Partition partition = (Partition) o;

        return oid.equals(partition.oid);
    }

    public int hashCode() {
        return oid.hashCode();
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public String toString() {
        return getPartitionType().toString() + "_" + oid + "(" + getDatabaseName() + ")";
    }

    @NotNull
    @Length(max = 100)
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @NotNull
    @Length(max = 50)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @NotNull
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getRootPassword() {
        return rootPassword;
    }

    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }

    @NotNull
    @Type(type = StringEnumType.TYPE)
    //@Type(type="org.narrative.network.core.cluster.partition.PartitionType$Type")
    @Column(columnDefinition = PartitionType.ENUM_FIELD_TYPE)
    public PartitionType getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(PartitionType partitionType) {
        this.partitionType = partitionType;
    }

    @Transient
    public String getTypeString() {
        return getPartitionType().toString();
    }

    @Transient
    public String getDisplayName() {
        return server + " - " + databaseName + " - " + getPartitionType().toString();
    }

    @Transient
    public GDataSource getDataSource() {
        return PartitionConnectionPool.getPartitionConnectionPool(this).getDataSource();
    }

    @Transient
    public DatabaseResources getDatabaseResources() {
        return PartitionConnectionPool.getPartitionConnectionPool(this).getDatabaseResources();
    }

    @Transient
    public DatabaseResources getBootstrapDatabaseResources() {
        // this should only be used for initializing OID generation and bootstrap patches.
        assert getPartitionType().isGlobal() : "Can only use bootstrap database resources for the global database!";
        return PartitionConnectionPool.getOneOffDatabaseResources(this);
    }

    @Transient
    public DatabaseResources getResolvedDatabaseResources() {
        if (PatchRunner.isBootstrapPatching()) {
            assert getPartitionType().isGlobal() : "Can only use the global partition when doing bootstrap patches!";
            return getBootstrapDatabaseResources();
        }
        return getDatabaseResources();
    }

    public void dropPartitionDatabase(String server, String rootPassword, boolean ignoreError) {
        Connection con = null;
        try {
            con = createOneOffNonDatabaseConnnection(server, "root", rootPassword);
            Statement stmt = con.createStatement();
            stmt.execute("drop database " + databaseName);

            if (!username.equalsIgnoreCase("root")) {
                stmt.execute("drop user " + username);
            }
        } catch (SQLException e) {
            if (ignoreError) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Error dropping database. Probably doesn't exist yet? Ignoring.  \"" + e.getMessage() + "\"");
                }
            } else {
                throw UnexpectedError.getRuntimeException("Unable to drop database " + databaseName, e);
            }
        } finally {
            PersistenceUtil.closeConnection(con);
        }

    }

    public void createPartitionDatabase(String server, String rootPassword) {
        List<String> sqlQueries = newArrayList(4);

        sqlQueries.add("create database " + databaseName);
        if (!username.equalsIgnoreCase("root")) {
            Set<String> hosts = new LinkedHashSet<>();
            hosts.add("%");
            for (String host : hosts) {
                sqlQueries.add("CREATE USER '" + username + "'@'" + host + "' IDENTIFIED BY '" + password + "'");

                sqlQueries.add("GRANT ALL PRIVILEGES ON `" + databaseName + "`.* TO '" + username + "'@'" + host + "'");

                // finally, add any partition type specific queries (such as grants for the global db -> reputation db)
                sqlQueries.addAll(getPartitionType().getExtraInstallRootSqlQueries(host, this));
            }
        }
        issueSqlAsRoot(server, rootPassword, sqlQueries);
    }

    public void issueSqlAsRoot(String server, String rootPassword, List<String> sqlQueries) {
        //create the database
        Connection con = null;
        try {
            con = createOneOffNonDatabaseConnnection(server, "root", rootPassword);
            Statement stmt = con.createStatement();
            for (String sqlQuery : sqlQueries) {
                stmt.execute(sqlQuery);
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Error creating database for partition " + databaseName + ".  Does the database already exist?", e);
        } finally {
            PersistenceUtil.closeConnection(con);
        }
    }

    public void createDatabaseTables(boolean dropFirst, boolean continueOnErrors) {
        GConnection con = null;
        try {
            con = getDataSource().getConnection();
            NarrativeAnnotationConfiguration cfg = getPartitionType().getConfiguration();
            cfg.buildMappings();

            Dialect dialect = Dialect.getDialect(cfg.getProperties());

            //Get the SQL commands from the resource indicated by the partition type
            URL schemaScriptURL = partitionType.getSchemaBaselineScriptURL();

            //Import the script
            logger.info("Importing resource " + schemaScriptURL.getPath() + " for partition " + toString());
            InputStream is = new BufferedInputStream(schemaScriptURL.openStream());
            importSqlScript(is);

            // get the additional install sql commands
            logger.info("Executing additional install commands");
            List<String> installSqlCommands = getPartitionType().getInstallSQLCommands();
            runStatements(con, installSqlCommands, continueOnErrors);

        } catch (SQLException | IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to create database", e);
        } finally {
            PersistenceUtil.closeConnection(con);
        }
    }

    public static void runStatements(Connection con, Collection<String> statements, boolean continueOnErrors) throws SQLException {
        runStatements(con, statements.toArray(new String[]{}), continueOnErrors);
    }

    private static void runStatements(Connection con, String[] statements, boolean continueOnErrors) throws SQLException {
        Statement stmt = con.createStatement();
        for (String statement : statements) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Running statement: " + statement);
                }
                stmt.execute(statement);
            } catch (SQLException se) {
                if (continueOnErrors) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Ignoring error in sql statment ", se);
                    }
                } else {
                    throw se;
                }
            }
        }
    }

    /**
     * Creates a non-connection-pool connection.  For use for one-off jobs like
     * creating databases where the normal connection pool with not suffice.
     *
     * @param user
     * @param password
     * @return
     * @throws SQLException
     */
    private static Connection createOneOffNonDatabaseConnnection(String server, String user, String password) throws SQLException {
        String url = PersistenceUtil.MySQLUtils.getJDBCURL(server, "", user, password);
        return DriverManager.getConnection(url);
    }

    public void createFederatedTable(String federatedTableCreateQueryName, Partition partitionContainingFederatedTable) {
        getDatabaseResources().createFederatedTable(federatedTableCreateQueryName, partitionContainingFederatedTable.getUsername(), partitionContainingFederatedTable.getPassword(), partitionContainingFederatedTable.getServer(), partitionContainingFederatedTable.getDatabaseName());
    }

    @Transient
    public PartitionKey getPartitionKey() {
        if (partitionKey == null) {
            partitionKey = new PartitionKey(this);
        }
        return partitionKey;
    }

    public static PartitionDAO dao() {
        return DAOImpl.getDAO(Partition.class);
    }

    public void importSqlScript(InputStream sqlScriptInputStream) {
        try {
            //prepare the
            List<String> command2 = new ArrayList<String>(10);
            command2.add(NetworkRegistry.getInstance().getMysqlBinaryPath());
            command2.add("-C");

            addCommandIdentityParameters(command2);

            command2.add("-D");
            command2.add(getDatabaseName());

            logger.info("Running SQL Import: " + IPStringUtil.getSeparatedList(command2, " "));

            ObjectTriplet<Boolean, String, String> out2 = RuntimeUtils.exec(command2, new RuntimeUtils.Options(sqlScriptInputStream));

            if (!out2.getOne()) {
                throw UnexpectedError.getRuntimeException("Error executing external process. out: \"" + out2.getTwo() + "\" err: \"" + out2.getThree() + "\"");
            }
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Error importing data", t);
        }
    }

    private void addCommandIdentityParameters(List<String> command) {
        command.add("-u" + getUsername());
        if (!isEmpty(getPassword())) {
            command.add("-p" + getPassword());
        }
        command.add("-h" + getServer());
    }

    @Transient
    public boolean isTablePresent(String tableName) {
        return getResolvedDatabaseResources().isTablePresent(getDatabaseName(), tableName);
    }

    @Transient
    public boolean isColumnPresent(String tableName, String columnName) {
        ResultSet rs = null;
        try {
            rs = getResolvedDatabaseResources().getResultSet("select count(*) from information_schema.COLUMNS t " + "where t.table_schema = ? and t.table_name = ? and t.column_name = ?", getDatabaseName(), tableName, columnName);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for column: " + columnName, e);
        } finally {
            PersistenceUtil.close(rs);
        }
        return false;
    }

    @Transient
    public boolean isIndexPresent(String tableName, String indexName) {
        ResultSet rs = null;
        try {
            rs = getResolvedDatabaseResources().getResultSet("select count(*) from information_schema.STATISTICS s " + "where s.table_schema = ? and s.table_name = ? and s.index_name = ?", getDatabaseName(), tableName, indexName);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for index: " + indexName, e);
        } finally {
            PersistenceUtil.close(rs);
        }
        return false;
    }

    @Transient
    public boolean isConstraintPresent(String tableName, String constraintName) {
        ResultSet rs = null;
        try {
            rs = getResolvedDatabaseResources().getResultSet("select count(*) from information_schema.TABLE_CONSTRAINTS s " + "where s.table_schema = ? and s.table_name = ? and s.constraint_name = ?", getDatabaseName(), tableName, constraintName);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for constraint: " + constraintName, e);
        } finally {
            PersistenceUtil.close(rs);
        }
        return false;
    }

    @Transient
    public String getDatabaseJdbcUrl() {
        assert partitionType.getDatabaseType().isMysql() : "Only MySQL dialect supported now!";
        return PersistenceUtil.MySQLUtils.getJDBCURL(server, databaseName, null, null);
    }

    public static void updateSingletonPartitionsInDb() {
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                Map<PartitionType, Set<Partition>> partitionMap = Partition.dao().getAllByTypeMap();
                for (PartitionType partitionType : PartitionType.getSingletonPartitionTypes()) {
                    Set<Partition> partitions = partitionMap.get(partitionType);
                    Partition singletonPartition = partitionType.getSingletonPartition();
                    if (partitions.isEmpty()) {
                        Partition.dao().save(singletonPartition);
                        continue;
                    }
                    assert partitions.size() == 1 : "Should always find exactly one partition for singleton partitions in the db! type/" + partitionType;
                    Partition partition = partitions.iterator().next();
                    partition.setDatabaseName(singletonPartition.getDatabaseName());
                    partition.setParameters(singletonPartition.getParameters());
                    partition.setPassword(singletonPartition.getPassword());
                    partition.setUsername(singletonPartition.getUsername());
                    partition.setServer(singletonPartition.getServer());
                }
                return null;
            }
        });
    }
}

