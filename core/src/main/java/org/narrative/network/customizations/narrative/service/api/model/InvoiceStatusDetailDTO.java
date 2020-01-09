package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@JsonValueObject
@JsonTypeName("InvoiceStatusDetail")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class InvoiceStatusDetailDTO {
    private InvoiceStatus status;
    private InvoiceDetailDTO invoice;
}
