package org.narrative.network.customizations.narrative.invoices.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Date: 2019-02-04
 * Time: 09:45
 *
 * @author jonmark
 */
public class InvoiceDAO extends GlobalDAOImpl<Invoice, OID> {
    public InvoiceDAO() {
        super(Invoice.class);
    }

    public List<OID> getExpiredInvoicedOids() {
        return getGSession().createNamedQuery("invoice.getExpiredInvoicedOids", OID.class)
                .setParameter("invoicedStatus", InvoiceStatus.INVOICED)
                .list();
    }

    public List<OID> getInvoiceOidsWithPendingNrvePayment() {
        return getGSession().createNamedQuery("invoice.getInvoiceOidsWithPendingNrvePayment", OID.class)
                .setParameter("invoicedStatus", InvoiceStatus.INVOICED)
                .list();
    }

    public InvoiceStatus getInvoiceStatus(OID invoiceOid) {
        return getGSession().createNamedQuery("invoice.getInvoiceStatus", InvoiceStatus.class)
                .setParameter("invoiceOid", invoiceOid)
                .uniqueResult();
    }

    public List<OID> getInvoiceOidsByStatusBefore(InvoiceStatus status, Instant before) {
        return getGSession().createNamedQuery("invoice.getInvoiceOidsByStatusBefore", OID.class)
                .setParameter("status", status)
                .setParameter("before", new Date(before.toEpochMilli()))
                .list();
    }
}
