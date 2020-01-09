package org.narrative.network.core.user.services;

import org.narrative.config.StaticConfig;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandlePaidInvoiceTaskBase;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;

/**
 * Date: 2019-02-06
 * Time: 13:27
 *
 * @author jonmark
 */
public class HandlePaidKycCertificationInvoiceTask extends HandlePaidInvoiceTaskBase {

    public HandlePaidKycCertificationInvoiceTask(Invoice invoice) {
        super(InvoiceType.KYC_CERTIFICATION, invoice);
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: let's gather a few objects so we can process this.
        User user = invoice.getUser();
        KycService kycService = StaticConfig.getBean(KycService.class);

        // jw: this should handle all of the processing for us!
        kycService.updateKycUserStatus(user.getUserKyc(), UserKycStatus.READY_FOR_VERIFICATION, UserKycEventType.PAID, null, null);

        return null;
    }
}
