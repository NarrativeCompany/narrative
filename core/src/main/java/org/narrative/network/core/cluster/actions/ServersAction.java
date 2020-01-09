package org.narrative.network.core.cluster.actions;

import org.narrative.network.core.cluster.actions.server.SystemMonitoringAction;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Aug 29, 2006
 * Time: 11:29:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServersAction extends ClusterAction {

    private int refreshInterval = 10;

    public String input() throws Exception {
        return INPUT;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Override
    public String getSubMenuResource() {
        return SystemMonitoringAction.SERVLET_STATUS_MENU_RESOURCE;
    }
}
