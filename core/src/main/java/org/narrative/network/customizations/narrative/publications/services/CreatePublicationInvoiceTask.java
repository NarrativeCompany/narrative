package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.DeleteInvoiceTask;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationPlanInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 15:48
 *
 * @author jonmark
 */
public class CreatePublicationInvoiceTask extends AreaTaskImpl<PublicationInvoice> {
    private final Publication publication;
    private final PublicationPlanType plan;

    public CreatePublicationInvoiceTask(Publication publication, PublicationPlanInput input) {
        assert exists(publication) : "Should always be provided a publication!";
        assert input != null && input.getPlan() != null : "Should always be provided a plan!";

        this.publication = publication;
        this.plan = input.getPlan();
    }

    @Override
    protected void validate(ValidationHandler validationHandler) {
        // jw: Let's ensure that the publication can use the requested plan (has not exceeded any of the limits for the
        //    plan they are trying to change to, and that it is available to them right now.
        if (!publication.getAvailablePlans().contains(plan)) {
            validationHandler.addFieldError(PublicationPlanInput.Fields.plan, "createPublicationInvoiceTask.invalidPlan");
        }
    }

    @Override
    protected PublicationInvoice doMonitoredTask() {
        // jw: before we process, let's lock on the Publication to ensure that no two invoicing process step on each other.
        publication.lockForInvoiceProcessing();

        // jw: the first thing we need to do is cancel any other open invoices that may already be out there.
        List<PublicationInvoice> openPublicationInvoices = PublicationInvoice.dao().getOpenForPublication(publication);
        for (PublicationInvoice openPublicationInvoice : openPublicationInvoices) {
            boolean deleted = getAreaContext().doAreaTask(new DeleteInvoiceTask(openPublicationInvoice.getInvoice()));

            // jw: the only reason a invoice would not have been deleted is if it was paid before we processed it.
            if (!deleted) {
                throw new ApplicationError(wordlet("createPublicationInvoiceTask.paidByPreviousInvoice"));
            }
        }

        BigDecimal price = plan.getPrice();

        // bl: if the publication is eligible for the wait list discount, then apply the discount to the base price.
        if(publication.isEligibleForWaitListDiscount()) {
            price = price.multiply(BigDecimal.ONE.subtract(PublicationWaitListEntry.DISCOUNT_PERCENTAGE));
            price = IPUtil.roundBigDecimalToNearestPenny(price);
        }

        // jw: Let's calculate the nrveAmount and then create the invoice with that. Later, these same values will be
        //     used to setup the FiatPayment which should guarantee ubiquity in values regardless of payment method.
        NrveValue nrveAmount = GlobalSettingsUtil.getGlobalSettings().getNrveValue(price, 2, RoundingMode.UP);

        Invoice invoice = new Invoice(
                InvoiceType.PUBLICATION_ANNUAL_FEE,
                publication.getOwner(),
                nrveAmount,
                price,
                // jw: We will only give the owner a short window to pay the invoice now that is has been created.
                Instant.now().plus(PublicationInvoice.INVOICE_PERIOD)
        );

        // jw: create the PublicationInvoice from the invoice created above.
        PublicationInvoice publicationInvoice = new PublicationInvoice(publication, invoice, plan);
        PublicationInvoice.dao().save(publicationInvoice);

        // jw: we need to create the FiatPayment so that the rates are all locked.
        FiatPayment fiatPayment = new FiatPayment(invoice);
        FiatPayment.dao().save(fiatPayment);

        // jw: unlike a niche auction invoice, this is short lived and does not contain either the invoice email or the
        //     reminder email.

        return publicationInvoice;
    }
}
