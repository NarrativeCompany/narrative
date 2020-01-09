package org.narrative.network.core.system;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Date: 5/18/11
 * Time: 2:54 PM
 *
 * @author brian
 */
public interface ThreadBucket {
    public String getName();

    public ThreadPoolExecutor getThreadPoolExecutor();
}
