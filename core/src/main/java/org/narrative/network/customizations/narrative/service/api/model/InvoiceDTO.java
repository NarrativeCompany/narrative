package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-02-04
 * Time: 20:21
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("Invoice")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class InvoiceDTO {
    private final OID oid;
    private final InvoiceType type;
}
