package org.narrative.common.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 20, 2008
 * Time: 11:00:59 AM
 */
public enum DatabaseType {
    MYSQL,
    MYSQL_MYISAM,
    POSTGRES;

    public boolean isMysql() {
        return this == MYSQL;
    }

    public boolean isMysqlMyisam() {
        return this == MYSQL_MYISAM;
    }

    public boolean isPostgres() {
        return this == POSTGRES;
    }

}
