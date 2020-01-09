package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 9:41 AM
 */
public enum InvoiceStatus implements IntegerEnum, NameForDisplayProvider {
    INVOICED(0)
    ,PAID(1)
    ,EXPIRED(2)
    ,CANCELED(5)
    ,CHARGEBACK(3)
    ,REFUNDED(4)
    ,PENDING_PRORATED_REFUND(6)
    ,REFUNDED_PRORATED(7)
    ;

    private final int id;

    InvoiceStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("invoiceStatus." + this);
    }

    public boolean isHasBeenPaid() {
        return isPaid() || isReversedPayment() || isPendingProratedRefund() || isRefundedProrated();
    }

    public boolean isReversedPayment() {
        return isChargeback() || isRefunded();
    }

    public boolean isInvoiced() {
        return this == INVOICED;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }

    public boolean isChargeback() {
        return this == CHARGEBACK;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public boolean isPendingProratedRefund() {
        return this == PENDING_PRORATED_REFUND;
    }

    public boolean isRefundedProrated() {
        return this == REFUNDED_PRORATED;
    }

    public static InvoiceStatus getReversedPaymentStatus(boolean forChargeback) {
        return forChargeback ? CHARGEBACK : REFUNDED;
    }
}