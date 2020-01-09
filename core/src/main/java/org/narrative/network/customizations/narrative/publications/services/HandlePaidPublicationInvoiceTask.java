package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.invoices.services.HandlePaidChannelInvoiceTaskBase;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.PublicationPaymentLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.PublicationPaymentType;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-08
 * Time: 16:05
 *
 * @author jonmark
 */
public class HandlePaidPublicationInvoiceTask extends HandlePaidChannelInvoiceTaskBase<PublicationInvoice, Publication> {

    public HandlePaidPublicationInvoiceTask(Invoice invoice) {
        super(InvoiceType.PUBLICATION_ANNUAL_FEE, invoice);
    }

    @Override
    protected Publication getChannelConsumer(PublicationInvoice publicationInvoice) {
        return publicationInvoice.getPublication();
    }

    @Override
    protected void handlePaidInvoice(PublicationInvoice publicationInvoice, Publication publication) {
        PublicationPlanType newPlan = publicationInvoice.getPlan();

        // jw: let's capture whether the publication invoice is for a plan upgrade before we make any changes to the publication.
        boolean isUpgrade = publicationInvoice.isForPlanUpgrade();

        // jw: For publications it's all about endDatetime which will depend on what kind of invoice this was. If it was
        //     a upgrade, or the endDatetime is in the past then the endDatetime should be a year from now, otherwise it
        //     should be a year from when the Publication's current plan ends.
        boolean isInTrial = publication.isInTrialPeriod();

        // jw: now that we have the planStartDatetime calculated, let's go ahead and set the new endDatetime a year from then.
        publication.setEndDatetime(publicationInvoice.getNewEndDatetime());

        // jw: the only other thing remaining is to set their new plan. All in all pretty simple stuff.
        publication.setPlan(newPlan);

        // jw: now tha the Publication is updated, we just need to schedule the reminder emails. First, unschedule any
        //     reminders that may have existed from before.
        ProcessPublicationExpiringJob.unschedule(publication);

        // jw: since the new jobs/triggers will be sharing the same names, let's flush before we schedule the new ones
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: now with that done we can schedule all the new emails.
        ProcessPublicationExpiringJob.schedule(publication);

        // jw: if this is an upgrade and the site had previously paid for a different plan then we need to issue a refund.
        if (isUpgrade) {
            Invoice previousPurchaseInvoice = publication.getChannel().getPurchaseInvoice();
            assert exists(previousPurchaseInvoice) : "upgrades should always have a purchase invoice on the channel already.";
            previousPurchaseInvoice.refund();
        }

        // jw: we need to create a payment ledger entry:
        LedgerEntry ledgerEntry = new LedgerEntry(getAreaContext().getAreaUserRlm(), LedgerEntryType.PUBLICATION_PAYMENT);
        ledgerEntry.setChannelForConsumer(publication);
        PublicationPaymentLedgerEntryMetadata metadata = ledgerEntry.getMetadata();
        metadata.setPlan(newPlan);
        metadata.setPaymentType(PublicationPaymentType.getPaymentType(isInTrial, isUpgrade));

        LedgerEntry.dao().save(ledgerEntry);
    }
}
