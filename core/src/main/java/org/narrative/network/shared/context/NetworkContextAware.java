package org.narrative.network.shared.context;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 9:38:53 PM
 */
public interface NetworkContextAware extends NetworkContextHolder {
    void setNetworkContext(NetworkContext networkContext);
}
