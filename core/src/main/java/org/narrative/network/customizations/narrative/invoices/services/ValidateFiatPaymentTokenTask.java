package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;

/**
 * Date: 2019-02-04
 * Time: 19:54
 *
 * @author jonmark
 */
public abstract class ValidateFiatPaymentTokenTask extends AreaTaskImpl<String> {
    private final InvoiceType invoiceType;
    private final BigDecimal usdAmount;
    protected final String paymentToken;

    public ValidateFiatPaymentTokenTask(InvoiceType invoiceType, BigDecimal usdAmount, String paymentToken) {
        this.invoiceType = invoiceType;
        this.usdAmount = usdAmount;
        this.paymentToken = paymentToken;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public String getPaymentToken() {
        return paymentToken;
    }
}
