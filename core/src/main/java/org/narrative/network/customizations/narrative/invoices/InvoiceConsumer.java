package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.OID;

import java.math.BigDecimal;

/**
 * Date: 2019-02-05
 * Time: 09:26
 *
 * @author jonmark
 */
public interface InvoiceConsumer {
    OID getOid();
    Invoice getInvoice();
    BigDecimal getNrveUsdPrice();
    String getInvoiceConsumerTypeName();
    String getConsumerDisplayName();
    String getConsumerDisplayUrl();
}
