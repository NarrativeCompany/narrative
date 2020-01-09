package org.narrative.network.customizations.narrative.niches.nicheauction;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionSecurityDepositDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-04-16
 * Time: 13:21
 *
 * @author jonmark
 */
@Getter
@Setter
@FieldNameConstants
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(
        name = "uidx_nicheAuctionSecurityDeposit_user_auction",
        columnNames = {NicheAuctionSecurityDeposit.COLUMN__USER, NicheAuctionInvoice.COLUMN__AUCTION}
)})
public class NicheAuctionSecurityDeposit implements DAOObject<NicheAuctionSecurityDepositDAO>, InvoiceConsumer {
    // jw: We cannot use the lombok constants for the class level annotations, so we need to create our own.

    public static final String FIELD__USER = "user";
    public static final String FIELD__AUCTION = "auction";

    public static final String COLUMN__USER = FIELD__USER + "_" + User.FIELD__OID__NAME;
    public static final String COLUMN__AUCTION = FIELD__AUCTION + "_" + NicheAuction.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = NicheAuctionSecurityDeposit.Fields.invoice)})
    private OID oid;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.ALL})
    @JoinColumn(name = NicheAuctionInvoice.Fields.oid)
    @ForeignKey(name = "fk_nicheAuctionSecurityDeposit_invoice")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheAuctionSecurityDeposit_auction")
    private NicheAuction auction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheAuctionSecurityDeposit_user")
    private User user;

    public NicheAuctionSecurityDeposit(Invoice invoice, NicheAuction auction) {
        assert exists(invoice) && invoice.getType().isNicheAuctionSecurityDeposit() : "Should always be provided an invoice for niche auction security deposit.";
        assert exists(auction) : "Should always be provided a auction.";

        this.invoice = invoice;
        this.user = invoice.getUser();
        this.auction = auction;
    }

    @Override
    public BigDecimal getNrveUsdPrice() {
        throw UnexpectedError.getRuntimeException("Should never call this for NicheAuctionSecurityDeposit!");
    }

    @Override
    public String getInvoiceConsumerTypeName() {
        throw UnexpectedError.getRuntimeException("Should never call this for NicheAuctionSecurityDeposit!");
    }

    @Override
    public String getConsumerDisplayName() {
        throw UnexpectedError.getRuntimeException("Should never call this for NicheAuctionSecurityDeposit!");
    }

    @Override
    public String getConsumerDisplayUrl() {
        throw UnexpectedError.getRuntimeException("Should never call this for NicheAuctionSecurityDeposit!");
    }

    public static NicheAuctionSecurityDepositDAO dao() {
        return NetworkDAOImpl.getDAO(NicheAuctionSecurityDeposit.class);
    }
}
