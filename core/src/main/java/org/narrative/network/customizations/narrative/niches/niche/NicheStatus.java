package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public enum NicheStatus implements IntegerEnum, NameForDisplayProvider {
    ACTIVE(0),
    SUGGESTED(1),
    FOR_SALE(2),
    REJECTED(3),
    PENDING_PAYMENT(4);

    private final int id;

    NicheStatus(int id) {
        this.id = id;
    }

    public static final Set<NicheStatus> APPROVED_STATUSES = Collections.unmodifiableSet(EnumSet.of(ACTIVE, FOR_SALE, PENDING_PAYMENT));

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("nicheStatus." + this);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isSuggested() {
        return this == SUGGESTED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isForSale() {
        return this == FOR_SALE;
    }

    public boolean isPendingPayment() {
        return this == PENDING_PAYMENT;
    }
}
