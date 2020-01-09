package org.narrative.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 16, 2005
 * Time: 12:03:50 PM
 * Maintains a threadsafe pool of sync objects.  For instance if you wanted to sync based on site OID you could just
 * create a static SyncObjectManager reference, then call getSyncObject.
 * <p>
 * SyncObjectManager will automatically clean up sync objects that haven't
 * been used for over an hour.
 * <p>
 * due to the auto-expiring nature of the SyncObjects in the SyncObjectManager,
 * you shouldn't create external references to SyncObjects returned.  you should
 * always just immediately synchronize on the object returned and never reference
 * the object again.  once the SyncObject hasn't been referenced for an hour,
 * the SyncObject can be removed by the SyncObjectManager, at which point your
 * local reference will no longer succesfully result in the synchronization
 * you are desiring.
 * <p>
 * similarly, you shouldn't use SyncObjectManager if you maintain a synchronized lock
 * on SyncObjects for longer than an hour, as the object can be removed
 * after it hasn't been accessed for an hour.
 */
public class SyncObjectManager<T> {

    /**
     * SyncObjects expire after an hour of inactivity (no accesses).
     */
    private static final int SYNC_OBJECT_EXPIRATION_MS = IPDateUtil.HOUR_IN_MS;
    /**
     * go through and cleanup expired sync objects once per hour.
     */
    private static final int SYNC_OBJECT_REMOVAL_PROCESS_INTERVAL_MS = IPDateUtil.HOUR_IN_MS;

    private final Map<T, SyncObject> syncObjects = Collections.synchronizedMap(new HashMap<T, SyncObject>());
    private long lastRemovalProcessTime = System.currentTimeMillis();

    /**
     * Gets a sync object which can be used to lock on
     *
     * @param key The name of the sync object
     * @return The sync object associted with that key.
     */
    public SyncObject getSyncObject(T key) {
        SyncObject lock = syncObjects.get(key);
        if (lock == null) {
            synchronized (syncObjects) {
                lock = syncObjects.get(key);
                if (lock == null) {
                    lock = new SyncObject(key);
                    syncObjects.put(key, lock);
                }
            }
        }
        // we've accessed the lock
        lock.onAccess();

        // before we go, remove any expired sync objects.
        // internally, this method will only actually do anything
        // once an hour.
        removeExpiredSyncObjects();

        return lock;
    }

    /**
     * remove any expired sync objects.
     * only actually removes objects once per hour
     */
    private void removeExpiredSyncObjects() {
        // only do this once per hour
        if (!isTimeToRemoveExpiredSyncObjects()) {
            return;
        }
        synchronized (syncObjects) {
            // only do this once per hour
            if (!isTimeToRemoveExpiredSyncObjects()) {
                return;
            }
            // iterate through the entries in the map and remove the entries
            // that have expired.
            Iterator<Map.Entry<T, SyncObject>> iter = syncObjects.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<T, SyncObject> entry = iter.next();
                // bl: can't remove the object here or else it will
                // screw up the Iterator!
                if (entry.getValue().hasExpired()) {
                    iter.remove();
                }
            }
            lastRemovalProcessTime = System.currentTimeMillis();
        }
    }

    /**
     * determine if we need to loop through and remove expired sync objects
     *
     * @return true if we need to remove expired sync objects.
     */
    private boolean isTimeToRemoveExpiredSyncObjects() {
        return (System.currentTimeMillis() >= (lastRemovalProcessTime + SYNC_OBJECT_REMOVAL_PROCESS_INTERVAL_MS));
    }

    public class SyncObject {
        private final T key;
        private long lastAccessed = System.currentTimeMillis();

        SyncObject(T key) {
            this.key = key;
        }

        public T getKey() {
            return key;
        }

        /**
         * each time this sync object is accessed, call this
         * method to update the last accessed timestamp.
         */
        private void onAccess() {
            lastAccessed = System.currentTimeMillis();
        }

        /**
         * expire items from the SyncObjectManager after they
         * have been idle for an hour.
         *
         * @return true if this SyncObject has expired (hasn't been
         * accessed in an hour). in this case, we can remove
         * the sync object.
         */
        private boolean hasExpired() {
            return (System.currentTimeMillis() >= (lastAccessed + SYNC_OBJECT_EXPIRATION_MS));
        }
    }
}