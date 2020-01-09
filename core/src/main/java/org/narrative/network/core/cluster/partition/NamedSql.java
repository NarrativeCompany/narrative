package org.narrative.network.core.cluster.partition;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 7, 2007
 * Time: 11:06:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class NamedSql {
    private final String name;
    private final String sql;

    public NamedSql(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    public String getName() {
        return name;
    }

    public String getSql() {
        return sql;
    }
}
