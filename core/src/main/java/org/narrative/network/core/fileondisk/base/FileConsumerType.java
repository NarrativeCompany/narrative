package org.narrative.network.core.fileondisk.base;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Apr 13, 2007
 * Time: 10:43:52 AM
 */
public enum FileConsumerType implements IntegerEnum, StringEnum {
    AREA_CONTENT(0, "AREA_CONTENT"),
    FILE_ON_DISK(3, "FILE_ON_DISK"),
    CUSTOM_GRAPHIC(11, "CUSTOM_GRAPHIC"),
    GRAEMLIN(12, "GRAEMLIN"),
    DIALOG(13, "DIALOG"),
    STOCK_AVATAR(2, "STOCK_AVATAR"),
    CUSTOM_PROFILE_FIELD_FILE(23, "CUSTOM_PROFILE_FIELD_FILE");

    private final int id;
    private final String idStr;

    FileConsumerType(int id, String idStr) {
        this.id = id;
        this.idStr = idStr;
    }

    public int getId() {
        return id;
    }

    public String getIdStr() {
        return idStr;
    }

    public boolean isFileOnDisk() {
        return this == FILE_ON_DISK;
    }

    public boolean isContent() {
        return this == AREA_CONTENT;
    }

    public boolean isGraemlin() {
        return this == GRAEMLIN;
    }

    public boolean isStockAvatar() {
        return this == STOCK_AVATAR;
    }

}
