package org.narrative.network.core.master.graemlins;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 13/12/2017
 * Time: 13:15
 */
public enum GraemlinType {
    IMAGE,
    EMOJI;

    public String getNameForDisplay() {
        return wordlet("GraemlinType." + this);
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isEmoji() {
        return this == EMOJI;
    }
}