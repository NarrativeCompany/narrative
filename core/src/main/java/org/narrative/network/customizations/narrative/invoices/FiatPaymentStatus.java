package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/27/18
 * Time: 11:33 AM
 */
public enum FiatPaymentStatus implements IntegerEnum {
    CALCULATED(0),
    PAID(1),
    CHARGEBACK(2),
    REFUNDED(3)
    ;

    private final int id;

    FiatPaymentStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isCalculated() {
        return this == CALCULATED;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isChargeback() {
        return this == CHARGEBACK;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public static FiatPaymentStatus getReversedPaymentStatus(boolean forChargeback) {
        return forChargeback ? CHARGEBACK : REFUNDED;
    }
}