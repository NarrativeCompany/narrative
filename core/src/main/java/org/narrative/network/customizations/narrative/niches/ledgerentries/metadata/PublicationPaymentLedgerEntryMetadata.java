package org.narrative.network.customizations.narrative.niches.ledgerentries.metadata;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryMetadata;
import org.narrative.network.customizations.narrative.publications.PublicationPaymentType;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;

/**
 * Date: 2019-08-22
 * Time: 10:05
 *
 * @author jonmark
 */
public interface PublicationPaymentLedgerEntryMetadata extends LedgerEntryMetadata {
    PublicationPlanType getPlan();
    void setPlan(PublicationPlanType plan);

    PublicationPaymentType getPaymentType();
    void setPaymentType(PublicationPaymentType paymentType);
}
