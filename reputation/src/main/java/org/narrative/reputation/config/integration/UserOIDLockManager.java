package org.narrative.reputation.config.integration;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.shared.event.Event;
import org.narrative.shared.event.reputation.BulkUserEvent;
import org.narrative.shared.event.reputation.UserEvent;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class UserOIDLockManager {
    private static final String BULK_USER_OID_LOCK_NAME = "BulkUserOIDLock";
    private static final String USER_OID_LOCK_NAME_PREFIX = "ReputationUserOIDLock_";

    private final RedissonClient redissonClient;
    private final ReputationProperties reputationProperties;

    public UserOIDLockManager(RedissonClient redissonClient, ReputationProperties reputationProperties) {
        this.redissonClient = redissonClient;
        this.reputationProperties = reputationProperties;
    }

    public Event createAndAcquireLock(Event event) {
        Lock lock;
        String oidString;
        if (event instanceof BulkUserEvent) {
            oidString = Joiner.on(',').join(((BulkUserEvent) event).getUserOidSet());
            if (log.isDebugEnabled()) {
                log.debug("Acquiring lock for OIDs {}", oidString);
            }
            lock = buildAndAcquireMultiUserOIDLock((BulkUserEvent) event, oidString);
            log.debug("Lock acquired for OIDs {}", oidString);
        } else {
            oidString = Long.toString(((UserEvent) event).getUserOid());
            log.debug("Acquiring lock for OID {}", oidString);
            lock = buildAndAcquireSingleUserOIDLock((UserEvent) event);
            log.debug("Lock acquired for OID {}", oidString);
        }

        /*
         * Register a transaction synchronization to unlock for this event after commit or rollback
         */
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                unlockLock(lock, oidString);
            }
        });

        return event;
    }

    private void unlockLock(Lock lock, String oidString) {
        if (lock != null) {
            lock.unlock();
            log.debug("Lock unlocked for OID {}", oidString);
        } else {
            log.error("Lock was null for OID {}!", oidString);
        }
    }

    private Lock buildAndAcquireSingleUserOIDLock(UserEvent userEvent) {
        //Get a fair lock for the current user OID
        RLock userLock = redissonClient.getFairLock(buildLockName(userEvent.getUserOid()));

        RuntimeException lockError = null;
        try {
            boolean locked = userLock.tryLock(
                    reputationProperties.getSi().getEvent().getMaxSingleUserOIDLockAcquireDuration().toMillis(),
                    reputationProperties.getSi().getEvent().getMaxSingleUserOIDLockOwnershipDuration().toMillis(),
                    TimeUnit.MILLISECONDS);
            if (!locked) {
                lockError = new RuntimeException("Timed out waiting for user OID lock - lock not acquired " + userEvent);
            }
        } catch (Exception e) {
            lockError = new RuntimeException("Error acquiring lock for user OID lock - lock not acquired " + userEvent);
        }

        if (lockError != null) {
            throw lockError;
        }

        return userLock;
    }

    private Lock buildAndAcquireMultiUserOIDLock(BulkUserEvent bulkUserEvent, String oidString) {
        // First lock on a mutex for multi user events - only one multi user event can attempt to acquire user OID
        // locks at a time.  This is to prevent deadlocks where multiple bulk lock requests with the same user OIDs
        // happen simultaneously.  Since bulk events are infrequent, this should not cause significant contention.
        RLock bulkLock = redissonClient.getFairLock(BULK_USER_OID_LOCK_NAME);
        boolean bulkLocked;
        RuntimeException lockError = null;
        try {
            log.debug("Acquiring bulk lock for event {}", bulkUserEvent);
            bulkLocked = bulkLock.tryLock(
                    reputationProperties.getSi().getEvent().getMaxBulkUserOIDLockAcquireDuration().toMillis(),
                    reputationProperties.getSi().getEvent().getMaxBulkUserOIDLockOwnershipDuration().toMillis(),
                    TimeUnit.MILLISECONDS);
            if (!bulkLocked) {
                lockError = new RuntimeException("Timed out waiting for bulkLock - lock not acquired " + bulkUserEvent);
            }
        } catch (Exception e) {
            lockError = new RuntimeException("Error acquiring bulkLock for bulk user event " + bulkUserEvent, e);
        }

        if (lockError != null) {
            throw lockError;
        }

        try {
            // Build a list of locks for all user OIDs
            Set<Long> userOidSet = bulkUserEvent.getUserOidSet();
            RLock[] lockArray = new RLock[userOidSet.size()];
            int idx = 0;
            for (Long userOid : userOidSet) {
                String lockName = buildLockName(userOid);
                lockArray[idx++] = redissonClient.getFairLock(lockName);
            }

            // Use a multi lock so we can lock/unlock all users atomically
            RedissonMultiLock multiLock = new RedissonMultiLock(lockArray);

            // Acquire the lock before unlocking the bulk lock - this will block other bulk operations while this
            // process is acquiring its locks
            try {
                log.debug("Acquiring locks for multilock: {}", oidString);
                boolean locked = multiLock.tryLock(
                        reputationProperties.getSi().getEvent().getMaxBulkUserOIDLockAcquireDuration().toMillis(),
                        reputationProperties.getSi().getEvent().getMaxBulkUserOIDLockOwnershipDuration().toMillis(),
                        TimeUnit.MILLISECONDS);
                if (!locked) {
                    lockError = new RuntimeException("Timed out waiting for user OID locks - lock not acquired");
                }
            } catch (Exception e) {
                lockError = new RuntimeException("Error acquiring user OID locks for bulk user event " + bulkUserEvent, e);
            }

            if (lockError != null) {
                throw lockError;
            }

            return multiLock;
        } finally {
            // Release the bulk lock
            bulkLock.unlock();
        }
    }

    private String buildLockName(long userOid) {
        return USER_OID_LOCK_NAME_PREFIX + Long.toString(userOid);
    }
}
