package org.narrative.shared.event.reputation;

import lombok.Getter;
import org.narrative.shared.event.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing event types and their respective queue names.
 */
@Getter
public enum ReputationEventType implements EventType<ReputationEventType, ReputationEvent> {
    CONDUCT_STATUS_EVENT(ConductStatusEvent.class.getSimpleName(), ConductStatusEvent.class),
    LIKE_EVENT(LikeEvent.class.getSimpleName(), CommentLikeEvent.class, ContentLikeEvent.class),
    KYC_VERIFICATION_EVENT(KYCVerificationEvent.class.getSimpleName(), KYCVerificationEvent.class),
    RATING_EVENT(RatingEvent.class.getSimpleName(), RatingEvent.class),
    RATING_CONSENSUS_CHANGED_EVENT(ConsensusChangedEvent.class.getSimpleName(), ConsensusChangedEvent.class),
    VOTE_ENDED_EVENT(VoteEndedEvent.class.getSimpleName(), VoteEndedEvent.class),
    NEGATIVE_QUALITY_EVENT(NegativeQualityEvent.class.getSimpleName(), NegativeQualityEvent.class);

    private static final Map<Class<? extends ReputationEvent>, ReputationEventType> eventTypeMap = new HashMap<>();
    private final Class<? extends ReputationEvent>[] eventClasses;
    private final String eventQueueName;

    static {
        for (ReputationEventType ret : ReputationEventType.values()) {
            for (Class<? extends ReputationEvent> reputationEventClass: ret.eventClasses) {
                eventTypeMap.put(reputationEventClass, ret);
            }
        }
    }

    /**
     * !!!Note first class in args is used for event queue name!!!
     */
    @SafeVarargs
    ReputationEventType(String queueName, Class<? extends ReputationEvent>... eventClasses) {
        this.eventClasses = eventClasses;
        if (eventClasses == null || eventClasses.length == 0) {
            throw new RuntimeException("No event class specified for value " + this);
        }
        this.eventQueueName = EventType.QUEUE_NAME_PREFIX + queueName;
    }

    public boolean isConductStatusEvent() {
        return this == CONDUCT_STATUS_EVENT;
    }

    public static ReputationEventType getEventType(Class<ReputationEvent> reputationEventClass) {
        ReputationEventType res = eventTypeMap.get(reputationEventClass);

        if (res == null) {
            throw new RuntimeException("Unmapped reputation event type: " + reputationEventClass.getName());
        }

        return res;
    }
}
