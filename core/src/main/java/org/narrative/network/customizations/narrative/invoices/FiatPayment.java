package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.GBigDecimal;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.invoices.dao.FiatPaymentDAO;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.paypal.services.PayPalCheckoutDetails;
import org.narrative.network.customizations.narrative.service.impl.invoice.FiatPaymentProcessorType;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 09:49
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FiatPayment extends InvoicePaymentBase<FiatPaymentDAO> {

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = Fields.invoice)})
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private FiatPaymentStatus status;

    @Type(type = IntegerEnumType.TYPE)
    private FiatPaymentProcessorType processorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_fiatPayment_paymentWalletTransaction")
    private WalletTransaction paymentWalletTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_fiatPayment_refundWalletTransaction")
    private WalletTransaction refundWalletTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_fiatPayment_reversalWalletTransaction")
    private WalletTransaction reversalWalletTransaction;

    @NotNull
    private BigDecimal usdAmount;
    @NotNull
    private BigDecimal feeUsdAmount;

    private transient GBigDecimal usdAmountForUi;
    private transient GBigDecimal feeUsdAmountForUi;
    private transient GBigDecimal totalUsdAmountForUi;

    /**
     * @deprecated for hibernate use only
     */
    public FiatPayment() {}


    public FiatPayment(Invoice invoice) {
        super(invoice);

        // jw: for publications we want to ensure that the fiat payment is using the same exact value as the invoice itself,
        //     since that is provided by the plan being purchased.
        if (invoice.getType().isPublicationAnnualFee()) {
            assert invoice.getUsdAmount() != null : "The invoice should have had a usdAmount setup prior to this being called!";
            updateUsdAmount(invoice.getUsdAmount());
            status = FiatPaymentStatus.CALCULATED;

        // jw: we need to calculate the current USD values if this invoice is deriving it's value from NRVE
        } else if (getNrveAmount()!=null) {
            // bl: use the NRVE-USD price that was locked at the start of the auction
            calculateUsdAmount(invoice.getInvoiceConsumer().getNrveUsdPrice());
            status = FiatPaymentStatus.CALCULATED;

        } else {
            assert invoice.getType().isImmediateFiatPaymentType() : "Expected immediate fiat payment type!";
            assert invoice.getUsdAmount() != null : "Should always have a USD Amount for immediate fiat payment types!";

            status = FiatPaymentStatus.PAID;
            this.usdAmount = invoice.getUsdAmount();
            this.feeUsdAmount = BigDecimal.ZERO;
            setupUiAmounts();
        }

        assert !exists(invoice.getFiatPayment()) : "The provided invoice should never have a fiat payment already! invoice/" + invoice.getOid();
        invoice.setFiatPayment(this);
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_fiatPayment_invoice")
    @PrimaryKeyJoinColumn
    public Invoice getInvoice() {
        return super.getInvoice();
    }

    protected void setupUiAmounts() {
        usdAmountForUi = new GBigDecimal(getUsdAmount());
        feeUsdAmountForUi = new GBigDecimal(getFeeUsdAmount());
        totalUsdAmountForUi = new GBigDecimal(getUsdAmount().add(getFeeUsdAmount()));
    }

    @Transient
    public UsdValue getUsdValue() {
        return new UsdValue(getUsdAmount());
    }

    @Transient
    public UsdValue getFeeUsdValue() {
        return new UsdValue(getFeeUsdAmount());
    }

    @Transient
    public GBigDecimal getUsdAmountForUi() {
        setupUiAmounts();
        return usdAmountForUi;
    }

    @Transient
    public GBigDecimal getFeeUsdAmountForUi() {
        setupUiAmounts();
        return feeUsdAmountForUi;
    }

    @Transient
    public GBigDecimal getTotalUsdAmountForUi() {
        setupUiAmounts();
        return totalUsdAmountForUi;
    }

    @Transient
    public BigDecimal getTotalUsdAmount() {
        return getTotalUsdAmountForUi().getValue();
    }

    @Transient
    public UsdValue getTotalUsdValue() {
        return new UsdValue(getTotalUsdAmount());
    }

    @Transient
    public PayPalCheckoutDetails getPayPalCheckoutDetails() {
        // jw: We only want to provide the PayPalCheckoutDetails if the Invoice is still pending payment (calculated)
        if (!hasBeenPaid() && getStatus().isCalculated()) {
            return getInvoice().getType().getPayPalCheckoutDetails(getTotalUsdAmount());
        }

        return null;
    }

    private static final BigDecimal CONVENIENCE_FEE_PERCENTAGE = new BigDecimal("0.15");

    public void calculateUsdAmount(BigDecimal nrveUsdPrice) {
        assert getNrveAmount() != null : "This method should only be called on FiatPayments that are providing a way to pay in lieu of NRVE.";

        // jw: let's delegate to the method below after determining the USD amount from the price provided.
        updateUsdAmount(calculateUsdAmount(nrveUsdPrice, getNrveAmount()));
    }

    public void updateUsdAmount(BigDecimal usdAmount) {
        // jw: let's ensure that we round these values up to the nearest cent when doing these calculations.
        setUsdAmount(usdAmount);
        setFeeUsdAmount(getUsdAmount().multiply(CONVENIENCE_FEE_PERCENTAGE).setScale(2, RoundingMode.CEILING));

        // jw: one last thing, let's go ahead and recalc the cached values for the UI
        setupUiAmounts();
    }

    public BigDecimal calculateUsdAmount(BigDecimal nrveUsdPrice, NrveValue nrveAmount) {
        BigDecimal nrveUsdAmount = nrveUsdPrice.multiply(nrveAmount.getValue()).setScale(2, RoundingMode.CEILING);
        // bl: force the USD price to always be a minimum of $75
        if(nrveUsdAmount.compareTo(NicheAuction.MINIMUM_BID_USD)<0) {
            return NicheAuction.MINIMUM_BID_USD;
        }
        return nrveUsdAmount;
    }

    @Override
    public boolean hasBeenPaid() {
        return super.hasBeenPaid() && getStatus().isPaid();
    }

    @Override
    @Transient
    public WalletTransactionStatus getInitialWalletTransactionStatus() {
        return WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT;
    }

    public static FiatPaymentDAO dao() {
        return NetworkDAOImpl.getDAO(FiatPayment.class);
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends InvoicePaymentBase.Fields {}
}
