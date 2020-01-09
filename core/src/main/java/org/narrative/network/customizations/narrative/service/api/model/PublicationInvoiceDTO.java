package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveValueDetail;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Date: 2019-08-20
 * Time: 10:48
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationInvoice")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PublicationInvoiceDTO {
    private final OID oid;

    private final PublicationPlanType plan;
    private final Instant newEndDatetime;
    // jw: this value is considered an estimate because there is always a chance that rewards will be processed after the
    //     the invoice is created, but before it is paid, in which case the refund amount will be less than this due to
    //     the rewards disbursement for the month.
    private final NrveValueDetail estimatedRefundAmount;
    private final InvoiceDetailDTO invoiceDetail;
}
