package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.NrvePaymentInput;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class PayInvoiceWithNrveTask extends PayInvoiceBaseTask<NrvePayment> {

    private final NrvePaymentInput nrvePaymentInput;
    private static final String NEO_ADDRESS_FIELD_NAME = "neoAddress";

    PayInvoiceWithNrveTask(NrvePaymentInput nrvePaymentInput, OID invoiceOid) {
        super(invoiceOid);
        this.nrvePaymentInput = nrvePaymentInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        String neoAddress = nrvePaymentInput.getNeoAddress();

        if (exists(invoice.getNrvePayment())) {
            throw UnexpectedError.getRuntimeException("should never have a payment already associated at the point where this is called!");
        }

        if (NeoUtils.validateNeoAddress(validationContext, NEO_ADDRESS_FIELD_NAME, neoAddress)) {
            NrvePayment existingPayment = NrvePayment.dao().getPendingPayment(neoAddress, invoice.getNrveAmount());
            if (exists(existingPayment)) {
                Invoice invoice = existingPayment.getInvoice();
                String errorMessage;
                if (invoice.getType().isNicheAuction()) {
                    NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();
                    Niche niche = auctionInvoice.getAuction().getNiche();

                    String nicheLink = IPHTMLUtil.getLink(niche.getDisplayUrl(), niche.getName());

                    if (invoice.isAccessibleByAreaRole(getAreaContext().getAreaRole())) {
                        errorMessage = wordlet("payInvoiceWithNrveTask.neoAddressAlreadyInUseForNiche.withInvoiceLink", invoice.getDisplayUrl(), nicheLink);
                    } else {
                        errorMessage = wordlet("payInvoiceWithNrveTask.neoAddressAlreadyInUseForNiche", nicheLink);
                    }
                } else {
                    if (invoice.isAccessibleByAreaRole(getAreaContext().getAreaRole())) {
                        errorMessage = wordlet("payInvoiceWithNrveTask.neoAddressAlreadyInUse.withInvoiceLink", invoice.getDisplayUrl());
                    } else {
                        errorMessage = wordlet("payInvoiceWithNrveTask.neoAddressAlreadyInUse");
                    }
                }

                validationContext.addFieldError(NEO_ADDRESS_FIELD_NAME, errorMessage);

            }
        }
    }

    @Override
    protected NrvePayment doMonitoredTask() {
        NrvePayment payment = invoice.getNrvePayment();
        String neoAddress = nrvePaymentInput.getNeoAddress();

        boolean isNew = !exists(payment);
        if (isNew) {
            payment = new NrvePayment(invoice, neoAddress);
            NrvePayment.dao().save(payment);

        } else if (!isEqual(payment.getFromNeoAddress(), neoAddress)) {
            payment.setFromNeoAddress(neoAddress);
        }

        // todo:remove once neo-python NEP-5 processing issues are resolved (#2087). until then, we can't rely on neo-python
        // notifying us of every payment. we'll have this as a mechanism to notify DevOps Leaders so that we can
        // investigate balances in the event we approach the due date without payment success.
        if (isNew && NetworkRegistry.getInstance().isProductionServer()) {
            sendDevOpsLeadersEmailAtEndOfThread();
        }

        return payment;
    }

    protected void sendDevOpsLeadersEmailAtEndOfThread() {
        // bl: generate the body while the current session is in scope so we have access to all Hibernate objects.
        String emailSubject;
        String emailBody;
        if (invoice.getType().isNicheAuction()) {
            NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();
            Niche niche = auctionInvoice.getAuction().getNiche();
            emailSubject = "New NicheAuction NrvePayment";
            emailBody = "New NicheAuction NrvePayment generated for niche/" + niche.getOid() + " " + niche.getDisplayUrl(true) + " - " + invoice.getNrveAmount().getFormattedWithSuffix() + " due from " + nrvePaymentInput.getNeoAddress() + " by " + invoice.getPaymentDueDatetime();

        } else if (invoice.getType().isPublicationAnnualFee()) {
            PublicationInvoice publicationInvoice = invoice.getInvoiceConsumer();
            Publication publication = publicationInvoice.getPublication();
            emailSubject = "New Publication NrvePayment";
            emailBody = "New Publication NrvePayment generated for publication/" + publication.getOid() + " " + publication.getDisplayUrl() + " - " + invoice.getNrveAmount().getFormattedWithSuffix() + " due from " + nrvePaymentInput.getNeoAddress() + " by " + invoice.getPaymentDueDatetime();

        } else {
            emailSubject = "New "+invoice.getType()+" NrvePayment";
            emailBody = "New NrvePayment generated for invoice/" + invoice.getOid() + "/" + invoice.getType() + ". " + invoice.getNrveAmount().getFormattedWithSuffix() + " due from " + nrvePaymentInput.getNeoAddress() + " by " + invoice.getPaymentDueDatetime();
        }
        PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> NetworkRegistry.getInstance().sendDevOpsStatusEmail(emailSubject, emailBody));
    }
}
