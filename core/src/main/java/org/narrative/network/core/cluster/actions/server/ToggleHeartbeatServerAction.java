package org.narrative.network.core.cluster.actions.server;

import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.system.HeartbeatServer;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.struts.NetworkResponses;

/**
 * User: barry
 * Date: Feb 18, 2010
 * Time: 10:17:46 PM
 */
public class ToggleHeartbeatServerAction extends ClusterAction {

    @Override
    public String input() throws Exception {
        if (HeartbeatServer.INSTANCE.isRunning()) {
            HeartbeatServer.INSTANCE.stopServer();
        } else {
            HeartbeatServer.INSTANCE.startServer(NetworkRegistry.getInstance().getHeartbeatServerPort());
        }

        return NetworkResponses.redirectResponse();
    }
}
