package org.narrative.shared.event.reputation;

public enum RatingType {
    AGE,
    QUALITY
    ;

    public boolean isAge() {
        return this==AGE;
    }

    public boolean isQuality() {
        return this==QUALITY;
    }
}
