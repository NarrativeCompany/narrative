package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.invoices.dao.InvoiceDAO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 09:44
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Invoice implements DAOObject<InvoiceDAO> {
    private static final String HAS_JIT_INITED_NICHE_AUCITON_INVOICE_PROPERTY = Invoice.class.getName() + "-HasJITInitedNicheAuctionInvoice";

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private InvoiceType type;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private InvoiceStatus status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_invoice_user")
    private User user;

    @NotNull
    private Timestamp invoiceDatetime;

    @NotNull
    private Timestamp paymentDueDatetime;

    // jw: this column represents the payment or expired datetime, and as a result is nullable
    private Timestamp updateDatetime;

    private Timestamp refundDatetime;

    // jw: note: these are nullable, though one of them should always be set.
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue nrveAmount;

    private BigDecimal usdAmount;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @ForeignKey(name = "fk_invoice_nrvePayment")
    private NrvePayment nrvePayment;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @ForeignKey(name = "fk_invoice_fiatPayment")
    private FiatPayment fiatPayment;

    /**
     * @deprecated for hibernate use only
     */
    public Invoice() {}

    private Invoice(InvoiceType type, User user) {
        assert type!=null : "Should always have a type at this point!";
        assert exists(user) : "Should always have a user at this point!";

        this.type = type;
        this.user = user;

        this.invoiceDatetime = now();
    }

    public Invoice(InvoiceType type, User user, NrveValue nrveAmount, long paymentPeriodInMs) {
        this(type, user);

        assert !type.isImmediateFiatPaymentType() : "This constructor should never be used for immediate fiat payment types!";
        assert nrveAmount!=null && nrveAmount.compareTo(NrveValue.ZERO) > 0 : "This constructor should only ever be used for Invoices for positive NrveValues.";
        assert paymentPeriodInMs > 0 : "Should always be providing some time for the user to pay.";

        this.status = InvoiceStatus.INVOICED;
        this.nrveAmount = nrveAmount;

        this.paymentDueDatetime = new Timestamp(invoiceDatetime.getTime() + paymentPeriodInMs);
    }

    public Invoice(InvoiceType type, User user, BigDecimal usdAmount) {
        this(type, user);

        assert type.isImmediateFiatPaymentType() : "this should only be used for immediate fiat payments!";
        assert usdAmount!=null && usdAmount.compareTo(BigDecimal.ZERO) > 0 : "This constructor should only ever be used for Invoices for positive USD amounts.";

        this.usdAmount = usdAmount;

        // jw: since this is being processed right now, let's go ahead and setup all the data accordingly
        this.status = InvoiceStatus.PAID;
        this.updateDatetime = this.invoiceDatetime;
        this.paymentDueDatetime = this.invoiceDatetime;
    }

    public Invoice(InvoiceType type, User user, NrveValue nrveAmount, BigDecimal usdAmount, Instant dueDatetime) {
        this(type, user);

        assert !type.isImmediateFiatPaymentType() : "this should only be used for non-immediate fiat payments!";
        assert usdAmount!=null && usdAmount.compareTo(BigDecimal.ZERO) > 0 : "Should always have a positive USD amount.";
        assert nrveAmount!=null && nrveAmount.compareTo(NrveValue.ZERO) > 0 : "Shuld always have a positive NrveValues.";
        assert dueDatetime != null && dueDatetime.isAfter(Instant.now()) : "The provided due datetime should always be in the future.";

        status = InvoiceStatus.INVOICED;
        this.nrveAmount = nrveAmount;
        this.usdAmount = usdAmount;

        // jw: since this is being processed right now, let's go ahead and setup all the data accordingly
        updateDatetime = invoiceDatetime;
        paymentDueDatetime = new Timestamp(dueDatetime.toEpochMilli());
    }

    public void updateStatus(InvoiceStatus status) {
        setStatus(status);
        setUpdateDatetime(now());
    }

    @Transient
    public UsdValue getUsdValue() {
        if (getUsdAmount()!=null) {
            return new UsdValue(getUsdAmount());
        }

        return null;
    }

    @Transient
    public NrvePayment getFreshNrvePayment() {
        NrvePayment payment = getNrvePayment();
        if (exists(payment)) {
            // jw: now that we have the lock, we need to refresh the object to make sure
            //     that if it was changed since we started that we have the latest version.
            NrvePayment.dao().refresh(payment);

            // jw: now, if the payment is paid we can short out. To reduce the window of failure
            //     lets go ahead and key this off of the existence of a transactionId.
            if (!exists(payment)) {
                return null;
            }
        }
        return payment;
    }

    public boolean isAccessibleByAreaRole(AreaRole areaRole) {
        // jw: only registered users can access invoices!
        if (!areaRole.isActiveRegisteredAreaUser()) {
            return false;
        }

        // jw: only the person the invoice is for can access the invoice currently. We may relax this and split this out to payment and viewing at some point!
        return isEqual(getUser(), areaRole.getUser());
    }

    @Transient
    public <T extends InvoiceConsumer> T getInvoiceConsumer() {
        return (T)getType().getInvoiceConsumer(this);
    }

    // jw: this one is a bit weird, but its necessary for providing data to the client when we are polling for payment
    //     processing. Basically, the goal is to provide the full invoice if the invoice expires or is paid, so that
    //     the UI can just update with the new values.
    @Transient
    public Invoice getInvoiceForStatusPolling() {
        if (getStatus().isInvoiced()) {
            return null;
        }

        return this;
    }

    /**
     * This utility method ensures that we are locking consistently in all places where it matters.
     */
    public void lockForProcessing() {
        // jw: before we lock the invoice we need to give the invoice type a chance to perform whatever global locks might
        //     be necessary.
        // note: adding this so that Publications, which support creating invoices more loosely and in an unconctrolled way,
        //       can lock on the publication and prevent race conditions between existing invoices and invoices which are
        //       being created.
        getType().lockForProcessing(this);

        Invoice.dao().refreshForLock(this);
    }

    @Transient
    public InvoicePaymentBase getPurchasePaymentResolved() {
        if (!getStatus().isHasBeenPaid()) {
            return null;
        }

        if (exists(getFiatPayment())) {
            assert getFiatPayment().hasBeenPaid() : "If the fiat payment has not been paid then it should be removed by now!";

            return getFiatPayment();
        }

        assert exists(getNrvePayment()) && getNrvePayment().hasBeenPaid(): "Expected that this invoice had been paid via a NrvePayment.";
        return getNrvePayment();
    }

    @Transient
    public String getDisplayUrl() {
        return ReactRoute.INVOICE.getUrl(getOid().toString());
    }

    @Transient
    public boolean isSupportsFiatPayment() {
        return getUser().isCanMakeFiatPayments();
    }

    public static Invoice getForEmailPreview(boolean isFullRefund) {
        Niche niche = Niche.getNicheForPreviewEmail(NicheStatus.REJECTED);
        Invoice invoice = new Invoice(InvoiceType.NICHE_AUCTION, networkContext().getUser(), new NrveValue(new BigDecimal("1890.55")), 100) {
            @Override
            public NicheAuctionInvoice getInvoiceConsumer() {
                return new NicheAuctionInvoice() {
                    @Override
                    public OID getOid() {
                        throw UnexpectedError.getRuntimeException("Shouldn't use this");
                    }

                    @Override
                    public Invoice getInvoice() {
                        throw UnexpectedError.getRuntimeException("Shouldn't use this");
                    }

                    @Override
                    public String getConsumerDisplayName() {
                        return niche.getName();
                    }

                    @Override
                    public String getConsumerDisplayUrl() {
                        return niche.getDisplayUrl();
                    }
                };
            }
        };
        invoice.updateStatus(InvoiceStatus.REFUNDED_PRORATED);
        NrvePayment nrvePayment = new NrvePayment(invoice, NeoWallet.dao().getNichePaymentNeoAddress());
        nrvePayment.setTransactionId("1234");
        nrvePayment.setTransactionDate(new Date());
        invoice.setNrvePayment(nrvePayment);
        WalletTransaction refundTransaction = new WalletTransaction(new Wallet(WalletType.NICHE_MONTH_REVENUE), networkContext().getUser().getWallet(), WalletTransactionType.NICHE_REFUND, WalletTransactionStatus.COMPLETED, RewardUtils.calculateNrveShare(invoice.getNrveAmount(), isFullRefund ? RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR : 9, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR));
        nrvePayment.setRefundWalletTransaction(refundTransaction);
        return invoice;
    }

    public void refund() {
        // bl: mark the purchaseInvoice as requiring a prorated refund so that we can easily identify it at the end of the month
        updateStatus(InvoiceStatus.PENDING_PRORATED_REFUND);
        Timestamp updateDatetime = getUpdateDatetime();
        setRefundDatetime(updateDatetime);
    }

    public static InvoiceDAO dao() {
        return NetworkDAOImpl.getDAO(Invoice.class);
    }
}
