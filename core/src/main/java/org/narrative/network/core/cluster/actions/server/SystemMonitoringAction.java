package org.narrative.network.core.cluster.actions.server;

import org.narrative.network.core.cluster.actions.ClusterAction;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 16, 2006
 * Time: 4:13:02 PM
 */
public class SystemMonitoringAction extends ClusterAction {

    public static final String SERVLET_STATUS_MENU_RESOURCE = "servletStatus";

    @Override
    public String getSubMenuResource() {
        return SERVLET_STATUS_MENU_RESOURCE;
    }
}
