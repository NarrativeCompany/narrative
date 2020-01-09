package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.services.HandlePaidKycCertificationInvoiceTask;
import org.narrative.network.core.user.services.HandleReversedKycCertificationPaymentTask;
import org.narrative.network.customizations.narrative.invoices.services.CreateWalletTransactionFromPaidInvoiceTask;
import org.narrative.network.customizations.narrative.invoices.services.CreateWalletTransactionFromPaidInvoiceTaskBase;
import org.narrative.network.customizations.narrative.invoices.services.HandlePaidInvoiceTaskBase;
import org.narrative.network.customizations.narrative.invoices.services.HandleReversedInvoicePaymentTaskBase;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.HandleExpiredNicheAuctionInvoiceTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.HandlePaidNicheAuctionInvoiceTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.HandleReversedNicheAuctionInvoicePaymentTask;
import org.narrative.network.customizations.narrative.paypal.services.PayPalCheckoutDetails;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionRevokedError;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.services.HandlePaidPublicationInvoiceTask;
import org.narrative.network.customizations.narrative.publications.services.HandleReversedPublicationInvoicePaymentTask;
import org.narrative.network.customizations.narrative.service.api.model.permissions.BidOnNichesRevokeReason;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 09:45
 *
 * @author jonmark
 */
public enum InvoiceType implements IntegerEnum {
    NICHE_AUCTION(0, WalletTransactionType.NICHE_PAYMENT, WalletTransactionType.NICHE_REFUND, NicheAuctionInvoice.class) {
        @Override
        public HandlePaidInvoiceTaskBase getPaidInvoiceHandler(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/"+invoice.getOid();

            return new HandlePaidNicheAuctionInvoiceTask(invoice);
        }

        @Override
        public CreateWalletTransactionFromPaidInvoiceTaskBase getCreateWalletTransactionTask(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/"+invoice.getOid();

            return new CreateWalletTransactionFromPaidInvoiceTask(ProratedRevenueType.NICHE_REVENUE, invoice);
        }

        @Override
        public AreaTaskImpl<Object> getExpiredInvoiceHandler(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/" + invoice.getOid();

            return new HandleExpiredNicheAuctionInvoiceTask(invoice);
        }

        @Override
        public HandleReversedInvoicePaymentTaskBase getReversedFiatPaymentHandler(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
            assert payment.getInvoice().getType() == this : "Using wrong invoiceType/"+payment.getInvoice().getType()+" for payment/"+payment.getOid();

            return new HandleReversedNicheAuctionInvoicePaymentTask(payment, forChargeback, originalInvoiceStatus);
        }

        @Override
        public void addTypeSpecificLedgerEntryFields(LedgerEntry ledgerEntry, Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/" + invoice.getOid();

            NicheAuctionInvoice auctionInvoice = invoice.getInvoiceConsumer();
            ledgerEntry.setChannelForConsumer(auctionInvoice.getAuction().getNiche());
        }

        @Override
        public NarrativeProperties.PayPal.ApiConfig getPayPalApiConfig() {
            return StaticConfig.getBean(NarrativeProperties.class).getPayPal().getChannelPayments();
        }

        @Override
        public String getNrvePaymentNeoAddress() {
            return NeoWallet.dao().getNichePaymentNeoAddress();
        }
    },
    KYC_CERTIFICATION(1, null, null, null) {
        @Override
        public boolean isImmediateFiatPaymentType() {
            return true;
        }

        @Override
        public boolean isImmediateFiatPaymentTypeAvailable(User user) {
            return exists(user) && user.getUserKyc().getKycStatus().isStartCheckEligible();
        }

        @Override
        public BigDecimal getImmediateFiatPaymentAmount(User user, NarrativeProperties props) {
            assert exists(user) : "Should never call this without a user!";

            UserKyc userKyc = user.getUserKyc();
            assert userKyc.getKycStatus().isStartCheckEligible() : "Should only ever get here for users eligible to start the KYC Certification process.";
            NarrativeProperties.PayPal.KycApiConfig apiConfig = props.getPayPal().getKycPayments();

            // bl: the only time you get the retry price is when your previous request was rejected.
            if (userKyc.getKycStatus().isRejected()) {
                return apiConfig.getRetryPrice();
            }
            // jw: if we have a promo price, we need to use that.
            if (apiConfig.getKycPromoPrice()!=null) {
                return apiConfig.getKycPromoPrice();
            }
            return apiConfig.getInitialPrice();
        }

        @Override
        public HandlePaidInvoiceTaskBase getPaidInvoiceHandler(Invoice invoice) {
            return new HandlePaidKycCertificationInvoiceTask(invoice);
        }

        @Override
        public HandleReversedInvoicePaymentTaskBase getReversedFiatPaymentHandler(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
            return new HandleReversedKycCertificationPaymentTask(payment, forChargeback, originalInvoiceStatus);
        }

        @Override
        public NarrativeProperties.PayPal.ApiConfig getPayPalApiConfig() {
            return StaticConfig.getBean(NarrativeProperties.class).getPayPal().getKycPayments();
        }
    },
    NICHE_AUCTION_SECURITY_DEPOSIT(2, null, null, NicheAuctionSecurityDeposit.class) {
        @Override
        public boolean isImmediateFiatPaymentType() {
            return true;
        }

        @Override
        public boolean isImmediateFiatPaymentTypeAvailable(User user) {
            assert exists(user) : "Should never get here without a user!";

            try {
                NarrativePermissionType.BID_ON_NICHES.checkRight(user.getLoneAreaRole());

            } catch (NarrativePermissionRevokedError e) {
                // jw: we only care about BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED
                if (e.getRevokeReason() != null && e.getRevokeReason() instanceof BidOnNichesRevokeReason) {
                    BidOnNichesRevokeReason reason = (BidOnNichesRevokeReason) e.getRevokeReason();

                    return reason.isSecurityDepositRequired();
                }
            }

            return false;
        }

        @Override
        public BigDecimal getImmediateFiatPaymentAmount(User user, NarrativeProperties props) {
            assert exists(user) : "Should never call this without a user!";

            NarrativeProperties.PayPal.AuctionConfig config = props.getPayPal().getAuctions();
            return config.getSecurityDepositPrice();
        }

        @Override
        public HandlePaidInvoiceTaskBase getPaidInvoiceHandler(Invoice invoice) {
            return null;
        }

        @Override
        public HandleReversedInvoicePaymentTaskBase getReversedFiatPaymentHandler(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
            return null;
        }

        @Override
        public NarrativeProperties.PayPal.ApiConfig getPayPalApiConfig() {
            return StaticConfig.getBean(NarrativeProperties.class).getPayPal().getChannelPayments();
        }
    },
    PUBLICATION_ANNUAL_FEE(3, WalletTransactionType.PUBLICATION_PAYMENT, WalletTransactionType.PUBLICATION_REFUND, PublicationInvoice.class) {
        @Override
        public HandlePaidInvoiceTaskBase getPaidInvoiceHandler(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/"+invoice.getOid();

            return new HandlePaidPublicationInvoiceTask(invoice);
        }

        @Override
        public CreateWalletTransactionFromPaidInvoiceTaskBase getCreateWalletTransactionTask(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/"+invoice.getOid();

            return new CreateWalletTransactionFromPaidInvoiceTask(ProratedRevenueType.PUBLICATION_REVENUE, invoice);
        }

        @Override
        public HandleReversedInvoicePaymentTaskBase getReversedFiatPaymentHandler(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus) {
            return new HandleReversedPublicationInvoicePaymentTask(payment, forChargeback, originalInvoiceStatus);
        }

        @Override
        public void addTypeSpecificLedgerEntryFields(LedgerEntry ledgerEntry, Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/" + invoice.getOid();

            PublicationInvoice publicationInvoice = invoice.getInvoiceConsumer();
            ledgerEntry.setChannelForConsumer(publicationInvoice.getPublication());
        }

        @Override
        public NarrativeProperties.PayPal.ApiConfig getPayPalApiConfig() {
            // jw: we will be using the same paypal backend for publications as we use for niches, so that fiat adjustments are
            //     easier to process on the backend.
            return StaticConfig.getBean(NarrativeProperties.class).getPayPal().getChannelPayments();
        }

        @Override
        public void lockForProcessing(Invoice invoice) {
            assert invoice.getType() == this : "Using wrong invoiceType/"+invoice.getType()+" for invoice/" + invoice.getOid();

            PublicationInvoice publicationInvoice = invoice.getInvoiceConsumer();
            publicationInvoice.getPublication().lockForInvoiceProcessing();
        }

        @Override
        public String getNrvePaymentNeoAddress() {
            return NeoWallet.dao().getPublicationPaymentNeoAddress();
        }
    }
    ;

    private final int id;
    private final WalletTransactionType paymentTransactionType;
    private final WalletTransactionType refundTransactionType;
    private final Class<? extends InvoiceConsumer> invoiceConsumerClass;

    InvoiceType(int id, WalletTransactionType paymentTransactionType, WalletTransactionType refundTransactionType, Class<? extends InvoiceConsumer> invoiceConsumerClass) {
        this.id = id;
        this.paymentTransactionType = paymentTransactionType;
        this.refundTransactionType = refundTransactionType;
        this.invoiceConsumerClass = invoiceConsumerClass;
    }

    @Override
    public int getId() {
        return id;
    }

    public NetworkDAOImpl getInvoiceConsumerDAO() {
        return (NetworkDAOImpl) DAOImpl.getDAO((Class) invoiceConsumerClass);
    }

    public WalletTransactionType getPaymentTransactionType() {
        assert paymentTransactionType!=null : "Should never use this method for InvoiceTypes that don't support transactions! type/" + this;
        return paymentTransactionType;
    }

    public WalletTransactionType getRefundTransactionType() {
        assert refundTransactionType!=null : "Should never use this method for InvoiceTypes that don't support transactions! type/" + this;
        return refundTransactionType;
    }

    public boolean isNicheAuction() {
        return this == NICHE_AUCTION;
    }

    public boolean isKycCertification() {
        return this == KYC_CERTIFICATION;
    }

    public boolean isNicheAuctionSecurityDeposit() {
        return this==NICHE_AUCTION_SECURITY_DEPOSIT;
    }

    public boolean isPublicationAnnualFee() {
        return this == PUBLICATION_ANNUAL_FEE;
    }

    public boolean isImmediateFiatPaymentType() {
        return false;
    }

    public boolean isImmediateFiatPaymentTypeAvailable(User user) {
        throw UnexpectedError.getRuntimeException("This should be overridden when necessary");
    }

    // jw: Including the user on this method because for KYC_CERTIFICATION we will need to derive the amount based on the users status!
    public BigDecimal getImmediateFiatPaymentAmount(User user, NarrativeProperties props) {
        throw UnexpectedError.getRuntimeException("Should override this method for types supporting immediate Fiat Payments.");
    }

    public InvoiceConsumer getInvoiceConsumer(Invoice invoice) {
        assert invoice.getType() == this : "Should only ever call this method with an invoice/"+invoice.getType()+" that matches this type/"+this;

        if (invoiceConsumerClass==null) {
            return null;
        }

        // jw: a bit funky, but should work.
        return (InvoiceConsumer) getInvoiceConsumerDAO().get(invoice.getOid());
    }

    public void deleteInvoiceConsumer(InvoiceConsumer invoiceConsumer) {
        assert exists(invoiceConsumer) : "We should always have a consumer provided!";
        assert invoiceConsumerClass != null : "Should only ever call this on types that have a defined invoice consumer type!";
        assert invoiceConsumer.getInvoice().getType() == this : "Should only ever call this method with an invoice consumer whose invoice/"+invoiceConsumer.getInvoice().getType()+" that matches this type/"+this;

        getInvoiceConsumerDAO().delete((DAOObject) invoiceConsumer);
    }

    public abstract HandlePaidInvoiceTaskBase getPaidInvoiceHandler(Invoice invoice);

    public CreateWalletTransactionFromPaidInvoiceTaskBase getCreateWalletTransactionTask(Invoice invoice) {
        return null;
    }

    public AreaTaskImpl<?> getExpiredInvoiceHandler(Invoice invoice) {
        throw UnexpectedError.getRuntimeException("Not supported for type/"+this);
    }

    public abstract HandleReversedInvoicePaymentTaskBase getReversedFiatPaymentHandler(FiatPayment payment, boolean forChargeback, InvoiceStatus originalInvoiceStatus);

    protected void addTypeSpecificLedgerEntryFields(LedgerEntry ledgerEntry, Invoice invoice) {
        // jw: by default, there is nothing to do here.
    }

    public void addLedgerEntryFields(LedgerEntry ledgerEntry, Invoice invoice) {
        ledgerEntry.setInvoice(invoice);

        addTypeSpecificLedgerEntryFields(ledgerEntry, invoice);
    }

    public abstract NarrativeProperties.PayPal.ApiConfig getPayPalApiConfig();

    public PayPalCheckoutDetails getPayPalCheckoutDetails(BigDecimal usdAmount) {
        return new PayPalCheckoutDetails(usdAmount, getPayPalApiConfig());
    }

    public PayPalCheckoutDetails getImmediatePaymentPayPalCheckoutDetails(User user, NarrativeProperties props) {
        assert isImmediateFiatPaymentType() : "Should only ever call this method for immediate InvoiceTypes. not/"+this;

        return getPayPalCheckoutDetails(getImmediateFiatPaymentAmount(user, props));
    }

    public void lockForProcessing(Invoice invoice) {
        // jw: by default, there is nothing to do here. Types with global locking concerns will override.
    }

    public boolean isDeleteExpiredInvoices() {
        return isPublicationAnnualFee();
    }

    public String getNrvePaymentNeoAddress() {
        throw UnexpectedError.getRuntimeException("Should never use this method for InvoiceTypes that don't support NRVE payments! type/" + this);
    }
}