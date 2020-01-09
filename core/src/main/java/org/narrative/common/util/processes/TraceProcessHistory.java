package org.narrative.common.util.processes;

import org.narrative.common.util.trace.TraceItem;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 1, 2006
 * Time: 5:17:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraceProcessHistory extends TraceItem {
    private String procName;

    public TraceProcessHistory(String procName) {
        this.procName = procName;
    }

    public String getProcName() {
        return procName;
    }

    public String getDisplayName() {
        return procName;
    }

    public void setProcName(String procName) {
        this.procName = procName;
    }
}
