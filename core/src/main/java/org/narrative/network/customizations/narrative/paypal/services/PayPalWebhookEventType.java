package org.narrative.network.customizations.narrative.paypal.services;

import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.services.ProcessReversedFiatPaymentTask;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import com.paypal.api.payments.Refund;
import com.paypal.base.rest.JSONFormatter;

import java.util.Map;

/**
 * Date: 2019-01-29
 * Time: 07:06
 *
 * @author jonmark
 */
public enum PayPalWebhookEventType implements StringEnum {
    SALE_REFUNDED("PAYMENT.SALE.REFUNDED")
    ,SALE_REVERSED("PAYMENT.SALE.REVERSED")
    ;

    private final String idStr;

    PayPalWebhookEventType(String idStr) {
        this.idStr = idStr;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public boolean isSaleRefunded() {
        return this == SALE_REFUNDED;
    }

    public boolean isSaleReversed() {
        return this == SALE_REVERSED;
    }

    public boolean isUsesRefundResource() {
        return isSaleRefunded() || isSaleReversed();
    }

    public AreaTaskImpl<FiatPayment> getEventProcessor(Map<String, Object> resourceData) {
        // jw: this base implementation will assume its for refunds, since that is shared between those two events.
        assert isUsesRefundResource() : "Expected to be called for a refund based type.";

        Refund refund = JSONFormatter.GSON.fromJson(JSONFormatter.GSON.toJsonTree(resourceData), Refund.class);

        assert refund!=null : "Should always have refund at this point!";

        return new ProcessReversedFiatPaymentTask(
                FiatPaymentProcessorType.PAYPAL
                // jw: the Sale.id is the transaction id
                , refund.getSaleId()
                // jw: a reversed payment from PayPal is a chargeback
                , isSaleReversed()
        );
    }
}
