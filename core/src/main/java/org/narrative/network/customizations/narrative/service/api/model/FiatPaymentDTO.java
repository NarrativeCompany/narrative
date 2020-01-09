package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.invoices.FiatPaymentStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * Value object representing a niche auction invoice fiat payment.
 */
@JsonValueObject
@JsonTypeName("FiatPayment")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class FiatPaymentDTO {
    private final OID oid;
    private final NrveValue nrveAmount;
    private final Date transactionDate;
    private final String transactionId;
    private final Boolean hasBeenPaid;

    private final UsdValue usdAmount;
    private final UsdValue feeUsdAmount;
    private final FiatPaymentStatus status;
    private final UsdValue totalUsdAmount;

    // jw: these are necessary for setting up and fulfilling the payment via PayPal. Will only be included when
    //     the payment has not been paid.
    private final PayPalCheckoutDetailsDTO payPalCheckoutDetails;
}
