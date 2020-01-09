package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.BitIntegerEnumType;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.dao.NrvePaymentDAO;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
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
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 09:48
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(
        name = "uidx_nrvePayment_fromNeoAddress_nrveAmount_paymentStatus"
        , columnNames = {NrvePayment.FIELD__FROM_NEO_ADDRESS, NrvePayment.FIELD__NRVE_AMOUNT, NrvePayment.FIELD__PAYMENT_STATUS}
)})
public class NrvePayment extends InvoicePaymentBase<NrvePaymentDAO> {

    // jw: because these field are used statically on class annotations we cannot use the lombok constants.
    public static final String FIELD__FROM_NEO_ADDRESS = "fromNeoAddress";
    public static final String FIELD__NRVE_AMOUNT = "nrveAmount";
    public static final String FIELD__PAYMENT_STATUS = "paymentStatus";

    public static final int NEO_ADDRESS_LENGTH = NeoUtils.NEO_ADDRESS_LENGTH;

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = Fields.invoice)})
    private OID oid;

    @NotNull
    @Length(min = NEO_ADDRESS_LENGTH, max = NEO_ADDRESS_LENGTH)
    private String fromNeoAddress;

    @Type(type = BitIntegerEnumType.TYPE)
    private NrvePaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_nrvePayment_paymentWalletTransaction")
    private WalletTransaction paymentWalletTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_nrvePayment_refundWalletTransaction")
    private WalletTransaction refundWalletTransaction;

    private boolean foundByExternalApi;

    /**
     * @deprecated for hibernate use only
     */
    public NrvePayment() {}


    public NrvePayment(Invoice invoice, String fromNeoAddress) {
        super(invoice);

        this.fromNeoAddress = fromNeoAddress;
        this.paymentStatus = NrvePaymentStatus.PENDING_PAYMENT;

        assert !exists(invoice.getNrvePayment()) : "The provided invoice should never have a payment already! invoice/" + invoice.getOid();
        invoice.setNrvePayment(this);
    }

    @Override
    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    public NrveValue getNrveAmount() {
        return super.getNrveAmount();
    }
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_nrvePayment_invoice")
    @PrimaryKeyJoinColumn
    public Invoice getInvoice() {
        return super.getInvoice();
    }

    @Transient
    public String getPaymentNeoAddress() {
        return getInvoice().getType().getNrvePaymentNeoAddress();
    }

    @Override
    @Transient
    public WalletTransactionStatus getInitialWalletTransactionStatus() {
        return WalletTransactionStatus.COMPLETED;
    }

    public static NrvePaymentDAO dao() {
        return NetworkDAOImpl.getDAO(NrvePayment.class);
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends InvoicePaymentBase.Fields {}
}
