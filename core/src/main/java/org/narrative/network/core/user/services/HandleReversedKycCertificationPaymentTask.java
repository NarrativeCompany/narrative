package org.narrative.network.core.user.services;

import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandleReversedInvoicePaymentTaskBase;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;

/**
 * Date: 2019-02-06
 * Time: 13:26
 *
 * @author jonmark
 */
public class HandleReversedKycCertificationPaymentTask extends HandleReversedInvoicePaymentTaskBase {

    public HandleReversedKycCertificationPaymentTask(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
        super(InvoiceType.KYC_CERTIFICATION, payment, forChargeback, originalInvoiceStatus);
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: let's gather a few objects so we can process this.
        User user = fiatPayment.getInvoice().getUser();
        KycService kycService = StaticConfig.getBean(KycService.class);

        if (forChargeback) {
            // jw: for chargebacks we want to revoke the users certification
            kycService.updateKycUserStatus(user.getUserKyc(), UserKycStatus.REVOKED, UserKycEventType.REVOKED, null, null);

        // jw: for refunds we want to reset the users status as long as they were not already revoked
        } else if (!user.getUserKyc().getKycStatus().isRevoked()) {
            NarrativeProperties narrativeProperties = StaticConfig.getBean(NarrativeProperties.class);
            // if the payment being refunded is more than the retry price, assume that the payment being
            // refunded is an initial submission. in that case, we will mark it as revoked and reset the user's
            // KYC status. if the payment being refunded is less than or equal to the retry price,
            // then we are going to assume the refund is for a resubmission, in which case we will leave the user's
            // KYC status untouched. this will allow us to selectively give user's refunds on retry
            // submissions on a case-by-case basis without also resetting their KYC status.
            if(fiatPayment.getUsdAmount().compareTo(narrativeProperties.getPayPal().getKycPayments().getRetryPrice()) > 0) {
                kycService.updateKycUserStatus(user.getUserKyc(), UserKycStatus.NONE, UserKycEventType.REFUNDED, null, null);
            }
        }

        return null;
    }
}
