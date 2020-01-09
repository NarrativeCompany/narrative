package org.narrative.shared.event.reputation;

public enum LikeEventType {
    LIKE,
    DISLIKE,
    DISLIKE_VIEWPOINT;

    public boolean isLike() {
        return this==LIKE;
    }

    public boolean isDislike() {
        return this==DISLIKE;
    }
}
