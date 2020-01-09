package org.narrative.shared.event.reputation;

import org.narrative.shared.jpa.type.IntEnum;

public enum ConductEventType implements IntEnum<ConductEventType> {
    FAILURE_TO_PAY_FOR_NICHE(0, 1),
    PAYMENT_CHARGEBACK(1, 2),
    NICHE_MODERATOR_REMOVED_BY_VOTE(2, 3),
    CONTENT_REMOVED_FOR_AUP_VIOLATION(3, 3),
    PUBLICATION_REMOVED_FOR_AUP_VIOLATION(4, 3),
    ;

    private final int id;
    private final int severity;

    ConductEventType(int id, int severity) {
        this.id = id;
        this.severity = severity;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isAupViolation() {
        return isContentRemovedForAupViolation() || isPublicationRemovedForAupViolation();
    }

    public boolean isContentRemovedForAupViolation() {
        return this==CONTENT_REMOVED_FOR_AUP_VIOLATION;
    }

    public boolean isPublicationRemovedForAupViolation() {
        return this==PUBLICATION_REMOVED_FOR_AUP_VIOLATION;
    }
}

