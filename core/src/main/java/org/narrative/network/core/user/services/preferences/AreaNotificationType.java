package org.narrative.network.core.user.services.preferences;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.LongBitmaskType;

/**
 * Date: Nov 11, 2010
 * Time: 1:04:57 PM
 *
 * @author brian
 */
public enum AreaNotificationType implements Bitmask<LongBitmaskType<AreaNotificationType>> {
    SOMEONE_FOLLOWED_ME(0x100000000L),
    SOMEONE_MENTIONS_ME(0x4000000000L);

    private final long bitmask;

    AreaNotificationType(long bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public long getBitmask() {
        return bitmask;
    }

    public LongBitmaskType<AreaNotificationType> getBitmaskType() {
        return new LongBitmaskType<>(bitmask);
    }
}
