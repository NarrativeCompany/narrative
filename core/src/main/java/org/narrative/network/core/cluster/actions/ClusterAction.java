package org.narrative.network.core.cluster.actions;

import org.narrative.network.shared.baseactions.NetworkAction;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Aug 29, 2006
 * Time: 9:21:57 AM
 */
public abstract class ClusterAction extends NetworkAction {
    public static final String CLUSTER_CP_MENU_RESOURCE = "cp";

    @Override
    public String getMenuResource() {
        return CLUSTER_CP_MENU_RESOURCE;
    }
}
