package org.narrative.network.customizations.narrative.reputation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 2018-12-13
 * Time: 12:34
 *
 * @author brian
 */
public enum ReputationLevel {
    HIGH(85),
    MEDIUM(50),
    LOW(0),
    CONDUCT_NEGATIVE(null)
    ;

    private final Integer minimumScore;

    ReputationLevel(Integer minimumScore) {
        this.minimumScore = minimumScore;
    }

    public static final List<ReputationLevel> SCORE_BASED_LEVELS_DESC;

    static {
        List<ReputationLevel> levels = new ArrayList<>(3);
        // bl: the enum's natural order is already sorted in descending order
        for (ReputationLevel value : values()) {
            if(value.minimumScore!=null) {
                levels.add(value);
            }
        }
        SCORE_BASED_LEVELS_DESC = Collections.unmodifiableList(levels);
    }

    public Integer getMinimumScore() {
        return minimumScore;
    }

    public boolean isHigh() {
        return this==HIGH;
    }

    public boolean isLow() {
        return this==LOW;
    }

    public boolean isConductNegative() {
        return this == CONDUCT_NEGATIVE;
    }
}
