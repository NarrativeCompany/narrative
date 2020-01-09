package org.narrative.network.customizations.narrative.posts;

import org.narrative.common.util.UnexpectedError;

/**
 * Date: 2019-03-07
 * Time: 12:00
 *
 * @author brian
 */
public enum QualityLevel {
    HIGH(80),
    MEDIUM(25),
    LOW(0),
    ;

    /**
     * Quality score minimum threshold (percent 0-100)
     */
    private final int minimumScore;

    QualityLevel(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public int getMinimumScore() {
        return minimumScore;
    }

    public boolean isLow() {
        return this==LOW;
    }

    public static QualityLevel getQualityLevelForScore(int score) {
        for (QualityLevel level : values()) {
            if(score>=level.getMinimumScore()) {
                return level;
            }
        }
        throw UnexpectedError.getRuntimeException("Failed to identify QualityLevel for score/" + score);
    }
}
