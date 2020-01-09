package org.narrative.network.core.user;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.IntBitmaskType;
import org.narrative.common.util.enums.StringEnum;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 3, 2006
 * Time: 1:39:53 PM
 */
public enum UserStatus implements Bitmask<IntBitmaskType<UserStatus>>, StringEnum {
    ACTIVE(0, "ACTIVE"),
    DEACTIVATED(2, "DEACTIVATED"),
    DELETED(4, "DELETED");

    private final int bitmask;
    private final String idStr;

    UserStatus(int bitmask, String idStr) {
        this.bitmask = bitmask;
        this.idStr = idStr;
    }

    public long getBitmask() {
        return bitmask;
    }

    public IntBitmaskType<UserStatus> getBitmaskType() {
        return new IntBitmaskType<>(bitmask);
    }

    public String getIdStr() {
        return idStr;
    }

    public boolean isDeactivated() {
        return this == DEACTIVATED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
