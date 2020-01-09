package org.narrative.shared.event.reputation;

import org.narrative.shared.jpa.type.IntEnum;

public enum NegativeQualityEventType implements IntEnum<NegativeQualityEventType> {
    NICHE_REJECTED_IN_BALLOT_BOX_OR_APPEAL(0, 10),
    APPEAL_NOT_UPHELD_BY_TRIBUNAL(1, 10),
    CHANGE_REQUEST_DENIED_BY_TRIBUNAL(2, 10);

    private final int id;
    private final int penalty;

    NegativeQualityEventType(int id, int penalty) {
        this.id = id;
        this.penalty = penalty;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getPenalty() {
        return penalty;
    }
}
