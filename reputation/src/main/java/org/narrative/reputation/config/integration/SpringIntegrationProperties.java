package org.narrative.reputation.config.integration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Spring integration properties for Reputation
 */
@Data
@Validated
public class SpringIntegrationProperties {
    private final Event event = new Event();

    @Data
    @Validated
    public static class Event {
        private final EventTypeConfig conductStatusEvent = new EventTypeConfig();
        private final EventTypeConfig likeEvent = new EventTypeConfig();
        private final EventTypeConfig kycVerifyEvent = new EventTypeConfig();
        private final EventTypeConfig ratingEvent = new EventTypeConfig();
        private final EventTypeConfig votingEndedEvent = new EventTypeConfig();
        private final EventTypeConfig negativeQualityEvent = new EventTypeConfig();
        private final EventTypeConfig consensusChangedEvent = new EventTypeConfig();

        /**
         * Maximum duration of a lock for de-duplication before it will be aquired by a new owner
         */
        @NotNull
        private Duration deduplicationLockDuration = Duration.of(5, ChronoUnit.MINUTES);
        /**
         * Maximum time to acquire a lock for a user OID
         */
        @NotNull
        private Duration maxSingleUserOIDLockAcquireDuration = Duration.of(60, ChronoUnit.SECONDS);
        /**
         * Maximum time to keep a lock for a user OID
         */
        @NotNull
        private Duration maxSingleUserOIDLockOwnershipDuration = Duration.of(60, ChronoUnit.SECONDS);
        /**
         * Maximum time to acquire a lock for bulk user OIDs
         */
        @NotNull
        private Duration maxBulkUserOIDLockAcquireDuration = Duration.of(5, ChronoUnit.MINUTES);
        /**
         * Maximum time to keep a lock for a bulk operation
         */
        @NotNull
        private Duration maxBulkUserOIDLockOwnershipDuration = Duration.of(5, ChronoUnit.MINUTES);
    }

    @Data
    public static class EventTypeConfig {
        /**
         * Polling interval for this poller
         */
        @NotNull
        private Duration pollInterval = Duration.of(100, ChronoUnit.MILLIS);
        /**
         * Maximum number of messages to pull from the queue in a single poll
         */
        @NotNull
        private int maxMessagesPerPoll = 1;
        /**
         * Number of threads for processing this type of event
         */
        @NotNull
        private int threadCount = 2;
        /** Should the poller start at application start up?  Set this to false for testing.
         *
         */
        @NotNull
        private boolean startPoller = true;
        /**
         * Transactional processing for this poller?
         */
        @NotNull
        private boolean transactionalProcessing = true;
    }
}
