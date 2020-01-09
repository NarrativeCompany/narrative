package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.*;

/**
 * Date: 2019-08-06
 * Time: 08:14
 *
 * @author jonmark
 */
public enum PublicationStatus implements IntegerEnum {
    ACTIVE(0),
    EXPIRED(1),
    // todo:post-v1.4.0: Remove this once we remove the DeleteRejectedPublicationsPatch patch.
    REJECTED(2)
    ;

    private final int id;

    PublicationStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }
}