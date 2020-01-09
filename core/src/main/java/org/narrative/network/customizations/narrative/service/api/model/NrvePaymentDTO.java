package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.NrvePaymentStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

/**
 * Value object representing a niche auction invoice payment.
 */
@JsonValueObject
@JsonTypeName("NrvePayment")
@Value
@Builder(toBuilder = true)
public class NrvePaymentDTO {
    private OID oid;
    private NrveValue nrveAmount;
    private Date transactionDate;
    private String transactionId;
    private Boolean hasBeenPaid;

    private String fromNeoAddress;
    private NrvePaymentStatus paymentStatus;

    private String paymentNeoAddress;
}
