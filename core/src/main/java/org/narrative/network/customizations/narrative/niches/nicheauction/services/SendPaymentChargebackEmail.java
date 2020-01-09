package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Date: 2019-01-29
 * Time: 15:57
 *
 * @author jonmark
 */
public class SendPaymentChargebackEmail extends SendSingleNarrativeEmailTaskBase {
    private FiatPayment payment;

    public SendPaymentChargebackEmail(FiatPayment payment) {
        super(payment.getInvoice().getUser());
        assert payment.getStatus().isChargeback() : "Should only ever call this for a payment that has been charged back! p/" + payment.getOid();

        this.payment = payment;
    }

    @Override
    public boolean isAlwaysSendEmail() {
        return true;
    }

    public FiatPayment getPayment() {
        return payment;
    }
}
