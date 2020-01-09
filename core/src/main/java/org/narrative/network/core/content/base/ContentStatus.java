package org.narrative.network.core.content.base;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.IntBitmaskType;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 26, 2006
 * Time: 9:08:47 AM
 */
public enum ContentStatus implements Bitmask<IntBitmaskType<ContentStatus>> {
    ACTIVE(0),
    DRAFT(1),
    DISABLED(4),
    DELETED(8);

    private final int bitmask;

    private ContentStatus(int bitmask) {
        this.bitmask = bitmask;
    }

    public long getBitmask() {
        return bitmask;
    }

    public IntBitmaskType<ContentStatus> getBitmaskType() {
        return new IntBitmaskType<ContentStatus>(bitmask);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
