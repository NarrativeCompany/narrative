package org.narrative.network.customizations.narrative.niches.nicheauction;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionInvoiceDAO;
import org.narrative.network.customizations.narrative.niches.services.DatetimeCountdownProvider;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.Date;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 9:38 AM
 */
@Getter
@Setter
@FieldNameConstants
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {NicheAuctionInvoice.COLUMN__BID, NicheAuctionInvoice.COLUMN__AUCTION})})
public class NicheAuctionInvoice implements DAOObject<NicheAuctionInvoiceDAO>, DatetimeCountdownProvider, InvoiceConsumer {
    // jw: We cannot use the lombok constants for the class level annotations, so we need to create our own.
    public static final String FIELD__BID = "bid";
    public static final String FIELD__AUCTION = "auction";

    public static final String COLUMN__BID = FIELD__BID + "_" + NicheAuctionBid.FIELD__OID__NAME;
    public static final String COLUMN__AUCTION = FIELD__AUCTION + "_" + NicheAuction.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = Fields.invoice)})
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheAuctionInvoice_auction")
    private NicheAuction auction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheAuctionInvoice_bid")
    private NicheAuctionBid bid;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.ALL})
    @JoinColumn(name = Fields.oid)
    @ForeignKey(name = "fk_nicheAuctionInvoice_invoice")
    private Invoice invoice;

    public static final long PAYMENT_PERIOD_IN_MS = IPDateUtil.DAY_IN_MS * 14;

    /**
     * @deprecated for hibernate use only
     */
    public NicheAuctionInvoice() { }

    public NicheAuctionInvoice(Invoice invoice, NicheAuction auction, NicheAuctionBid bid) {
        assert exists(invoice) && invoice.getType().isNicheAuction() : "Should always be provided an invoice for niche purchase.";
        assert exists(auction) : "Should always be provided a auction.";
        assert exists(bid) && isEqual(bid, auction.getLeadingBid()) : "Should always have a bid, that corresponds to the leading bid.";

        this.invoice = invoice;
        this.auction = auction;
        this.bid = bid;
    }

    public String getDisplayUrl() {
        return getInvoice().getDisplayUrl();
    }

    @Override
    public Date getCountdownTarget() {
        return getInvoice().getPaymentDueDatetime();
    }

    @Override
    public Date getCountdownEndingSoonTarget() {
        return DatetimeCountdownProvider.getCountdownEndingSoonTarget(this);
    }

    @Override
    public boolean isCountdownTargetLocked() {
        return true;
    }

    @Override
    public String getCountdownRefreshUrl() {
        return null;
    }

    @Override
    public BigDecimal getNrveUsdPrice() {
        return getAuction().getNrveUsdPrice();
    }

    @Override
    public String getInvoiceConsumerTypeName() {
        return wordlet("nicheAuctionInvoice.invoiceConsumerTypeName");
    }

    @Override
    public String getConsumerDisplayName() {
        return getAuction().getNiche().getName();
    }

    @Override
    public String getConsumerDisplayUrl() {
        return getAuction().getNiche().getDisplayUrl();
    }

    public static NicheAuctionInvoiceDAO dao() {
        return NetworkDAOImpl.getDAO(NicheAuctionInvoice.class);
    }
}
