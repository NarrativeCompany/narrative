package org.narrative.network.core.fileondisk.base;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 25, 2007
 * Time: 10:06:19 AM
 * To change this template use File | Settings | File Templates.
 */
public enum FileOnDiskStatus implements IntegerEnum {
    ACTIVE(0),
    PENDING_CONVERSION(1),
    TEMP_FILE(2);

    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_MISSING = "MISSING";
    public static final String STATUS_ACCESS_VIOLATION = "ACCESS_VIOLATION";

    private final int id;

    FileOnDiskStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isPendingConversion() {
        return this == PENDING_CONVERSION;
    }

    public boolean isTempFile() {
        return this == TEMP_FILE;
    }
}