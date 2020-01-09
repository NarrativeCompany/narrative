package org.narrative.network.customizations.narrative.niches.nicheauction;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 9:57 AM
 */
public enum BidStatus implements IntegerEnum {
    LEADING(0),
    OUTBID(1),
    FAILED_TO_PAY(3);

    private final int id;

    BidStatus(int id) {
        this.id = id;
    }

    public static final List<BidStatus> ACTIVE_STATUSES = Collections.unmodifiableList(Arrays.asList(LEADING, OUTBID));

    @Override
    public int getId() {
        return id;
    }

    public boolean isLeading() {
        return this == LEADING;
    }

    public boolean isOutbid() {
        return this == OUTBID;
    }

    public boolean isFailedToPay() {
        return this == FAILED_TO_PAY;
    }
}