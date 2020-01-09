package org.narrative.network.core.versioning;

import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.core.system.NetworkRegistry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 11, 2007
 * Time: 1:09:29 PM
 * Use factory methods to get different types of patch conditions.
 */
public abstract class PatchCondition {

    public abstract boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data);

    public abstract String getDescription();

    /**
     * Default condition which will always return true
     *
     * @return
     */
    public static PatchCondition getDefault() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return true;
            }

            public String getDescription() {
                return "Default patch condition is always true";
            }
        };
    }

    public static PatchCondition ifTableNotExists(final String tableName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !partition.isTablePresent(tableName);
            }

            public String getDescription() {
                return tableName + " table present";
            }
        };
    }

    public static PatchCondition ifTableExists(final String tableName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return partition.isTablePresent(tableName);
            }

            public String getDescription() {
                return tableName + " table not present";
            }
        };
    }

    public static PatchCondition ifColumnNotExists(final String tableName, final String columnName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !partition.isColumnPresent(tableName, columnName);
            }

            public String getDescription() {
                return tableName + "." + columnName + " column present";
            }
        };
    }

    public static PatchCondition ifColumnExists(final String tableName, final String columnName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return partition.isColumnPresent(tableName, columnName);
            }

            public String getDescription() {
                return tableName + "." + columnName + " column not present";
            }
        };
    }

    public static PatchCondition ifIndexNotExists(final String tableName, final String indexName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !partition.isIndexPresent(tableName, indexName);
            }

            public String getDescription() {
                return tableName + ":" + indexName + " index present";
            }
        };
    }

    public static PatchCondition ifIndexExists(final String tableName, final String indexName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return partition.isIndexPresent(tableName, indexName);
            }

            public String getDescription() {
                return tableName + ":" + indexName + " index not present";
            }
        };
    }

    public static PatchCondition ifConstraintExists(final String tableName, final String constraintName) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return partition.isConstraintPresent(tableName, constraintName);
            }

            public String getDescription() {
                return tableName + ":" + constraintName + " constraint not present";
            }
        };
    }

    public static PatchCondition ifLocalServer() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return NetworkRegistry.getInstance().isLocalServer();
            }

            public String getDescription() {
                return "not local server";
            }
        };
    }

    public static PatchCondition ifNotLocalServer() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !NetworkRegistry.getInstance().isLocalServer();
            }

            public String getDescription() {
                return "local server";
            }
        };
    }

    public static PatchCondition ifNotProductionServer() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !NetworkRegistry.getInstance().isProductionServer();
            }

            public String getDescription() {
                return "production server";
            }
        };
    }

    public static PatchCondition ifProductionServer() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return NetworkRegistry.getInstance().isProductionServer();
            }

            public String getDescription() {
                return "not production server";
            }
        };
    }

    private static int getQueryResultCount(Partition partition, String query) {
        ResultSet rs = null;
        try {
            rs = partition.getResolvedDatabaseResources().getResultSet(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for query result count: " + query, e);
        } finally {
            PersistenceUtil.close(rs);
        }
    }

    public static PatchCondition ifQueryReturnsCountGreaterThanZero(final String query) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return getQueryResultCount(partition, query) > 0;
            }

            public String getDescription() {
                return "query \"" + query + "\" has result count equal to zero";
            }
        };
    }

    public static PatchCondition ifQueryReturnsCountEqualToZero(final String query) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return getQueryResultCount(partition, query) == 0;
            }

            public String getDescription() {
                return "query \"" + query + "\" has result count greater than zero";
            }
        };
    }

    private static boolean doesQueryReturnResults(Partition partition, String query) {
        ResultSet rs = null;
        try {
            rs = partition.getResolvedDatabaseResources().getResultSet(query);
            return rs.next();
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to test for query results: " + query, e);
        } finally {
            PersistenceUtil.close(rs);
        }
    }

    public static PatchCondition ifQueryReturnsNoResults(final String query) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !doesQueryReturnResults(partition, query);
            }

            public String getDescription() {
                return "query \"" + query + "\" has results";
            }
        };
    }

    public static PatchCondition ifQueryReturnsResults(final String query) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return doesQueryReturnResults(partition, query);
            }

            public String getDescription() {
                return "query \"" + query + "\" has no results";
            }
        };
    }

    public static PatchCondition ifClusterId(final String clusterId) {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return IPStringUtil.isStringEqualIgnoreCase(NetworkRegistry.getInstance().getClusterId(), clusterId);
            }

            public String getDescription() {
                return "not cluster/" + clusterId;
            }
        };
    }

    public static PatchCondition ifEncryptionEnabled() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return Encryption.INSTANCE.isEnabled();
            }

            public String getDescription() {
                return "encryption disabled";
            }
        };
    }

    public static PatchCondition ifEncryptionDisabled() {
        return new PatchCondition() {
            public boolean shouldPatchRun(NamedPatch patch, Partition partition, Properties data) {
                return !Encryption.INSTANCE.isEnabled();
            }

            public String getDescription() {
                return "encryption enabled";
            }
        };
    }
}