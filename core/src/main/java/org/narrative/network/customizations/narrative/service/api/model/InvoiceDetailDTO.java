package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

/**
 * Date: 2019-02-04
 * Time: 13:14
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("InvoiceDetail")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class InvoiceDetailDTO {
    private final OID oid;

    private final InvoiceType type;
    private final InvoiceStatus status;

    private final NrveValue nrveAmount;
    private final UsdValue usdAmount;

    private final Timestamp invoiceDatetime;
    private final Timestamp paymentDueDatetime;
    private final Timestamp updateDatetime;

    private final NrvePaymentDTO nrvePayment;
    private final FiatPaymentDTO fiatPayment;

    private final NicheAuctionInvoiceDTO nicheAuctionInvoice;
}
