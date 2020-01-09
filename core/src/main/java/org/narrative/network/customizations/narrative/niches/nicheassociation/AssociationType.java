package org.narrative.network.customizations.narrative.niches.nicheassociation;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/02/2018
 * Time: 15:04
 */
public enum AssociationType implements IntegerEnum {
    OWNER(0),
    BIDDER(1);

    private final int id;

    AssociationType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isOwner() {
        return this == OWNER;
    }

    public boolean isBidder() {
        return this == BIDDER;
    }
}
