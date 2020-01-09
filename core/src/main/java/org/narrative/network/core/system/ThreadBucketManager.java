package org.narrative.network.core.system;

import java.util.Collections;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 5/18/11
 * Time: 2:50 PM
 *
 * @author brian
 */
public enum ThreadBucketManager {
    INSTANCE;

    private final Set<ThreadBucket> threadBuckets = newLinkedHashSet();

    public void addThreadBucket(ThreadBucket threadBucket) {
        threadBuckets.add(threadBucket);
    }

    public void removeThreadBucket(ThreadBucket threadBucket) {
        threadBuckets.remove(threadBucket);
    }

    public Set<ThreadBucket> getAllThreadBuckets() {
        return Collections.unmodifiableSet(threadBuckets);
    }
}
