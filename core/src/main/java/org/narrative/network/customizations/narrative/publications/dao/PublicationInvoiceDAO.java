package org.narrative.network.customizations.narrative.publications.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

/**
 * Date: 2019-07-23
 * Time: 10:37
 *
 * @author jonmark
 */
public class PublicationInvoiceDAO extends GlobalDAOImpl<PublicationInvoice, OID> {
    public PublicationInvoiceDAO() {
        super(PublicationInvoice.class);
    }

    public List<PublicationInvoice> getOpenForPublication(Publication publication) {
        return getGSession().createNamedQuery("publicationInvoice.getOpenForPublication", PublicationInvoice.class)
                .setParameter("publication", publication)
                .setParameter("invoicedStatus", InvoiceStatus.INVOICED)
                .list();
    }
}
