package org.narrative.network.shared.replies.services;

import org.narrative.common.util.enums.StringEnum;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 5/15/15
 * Time: 1:57 PM
 */
public enum ReplySortOrder implements StringEnum {
    NEWEST_TO_OLDEST("newest"),
    OLDEST_TO_NEWEST("oldest"),
    POPULARITY("popular");

    private final String idStr;

    ReplySortOrder(String idStr) {
        this.idStr = idStr;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public String getNameForDisplay() {
        return wordlet(newString("replySortOrder.", this));
    }

    public boolean isNewestToOldest() {
        return this == NEWEST_TO_OLDEST;
    }

    public boolean isOldestToNewest() {
        return this == OLDEST_TO_NEWEST;
    }

    public boolean isPopular() {
        return this == POPULARITY;
    }

}
