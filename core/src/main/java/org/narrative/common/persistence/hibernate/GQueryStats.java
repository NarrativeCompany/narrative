package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.trace.TraceItem;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 30, 2006
 * Time: 3:49:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class GQueryStats extends TraceItem {
    private String select;
    private String rest;
    private List<Object> params;

    public GQueryStats(String query, List<Object> params) {
        makePretty(query);
        this.params = params;
    }

    public String getDisplayName() {
        return "Query";
    }

    public GQueryStats(String query) {
        this(query, Collections.emptyList());
    }

    public List<Object> getParams() {
        return params;
    }

    public String getSelect() {
        return select;
    }

    public String getRest() {
        return rest;
    }

    private void makePretty(String sql) {
        int fromStart = sql.indexOf(" from ");
        if (fromStart == -1) {
            select = sql;
            rest = "";
        } else {
            select = sql.substring(0, fromStart);
            rest = sql.substring(fromStart + 1);

            select = select.replaceAll("select ", "<b>select</b> ");
            rest = rest.replaceAll("from ", "<br/><b>from</b> ").replaceAll(" inner join ", "<br/><b>inner join</b> ").replaceAll(" left outer join", "<br/><b>left outer join</b> ").replaceAll(" where ", "<br/><b>where</b> ").replaceAll(" group by ", "<br/><b>group by</b> ").replaceAll(" order by ", "<br/><b>order by</b> ").replaceAll(" having ", "<br/><b>having</b> ");
        }
    }
}
