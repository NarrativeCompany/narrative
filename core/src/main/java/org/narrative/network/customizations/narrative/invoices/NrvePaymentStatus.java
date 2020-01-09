package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 9:32 AM
 */
public enum NrvePaymentStatus implements IntegerEnum {
    PENDING_PAYMENT(0);

    private final int id;

    NrvePaymentStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}