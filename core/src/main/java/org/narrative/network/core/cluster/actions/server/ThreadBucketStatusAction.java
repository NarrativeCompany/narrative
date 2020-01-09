package org.narrative.network.core.cluster.actions.server;

import org.narrative.network.core.system.ThreadBucket;
import org.narrative.network.core.system.ThreadBucketManager;

import java.util.Collection;
import java.util.Set;

/**
 * Date: Feb 22, 2008
 * Time: 9:42:04 AM
 *
 * @author brian
 */
public class ThreadBucketStatusAction extends SystemMonitoringAction {

    public String input() throws Exception {
        return INPUT;
    }

    public Set<org.narrative.common.util.ThreadBucket> getOldThreadBuckets() {
        return org.narrative.common.util.ThreadBucket.getAllThreadBuckets();
    }

    public Collection<ThreadBucket> getThreadBuckets() {
        return ThreadBucketManager.INSTANCE.getAllThreadBuckets();
    }

}
