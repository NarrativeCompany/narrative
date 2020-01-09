package org.narrative.network.customizations.narrative.invoices;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import java.util.Date;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-02-04
 * Time: 09:57
 *
 * @author jonmark
 */
@Getter
@Setter
@FieldNameConstants
@MappedSuperclass
@NoArgsConstructor
public abstract class InvoicePaymentBase <T extends DAO> implements DAOObject<T> {

    public InvoicePaymentBase(Invoice invoice) {
        this.invoice = invoice;
        this.nrveAmount = invoice.getNrveAmount();
    }

    // jw:note: the OID is defined on each implementor, since Hibernate does not recognize it otherwise.

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @PrimaryKeyJoinColumn
    private Invoice invoice;

    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue nrveAmount;

    @Column(length = 64, unique = true)
    private String transactionId;

    // jw: note: this is a Date because brian will be setting this value directly via SQL for NrvePayments
    private Date transactionDate;

    protected InvoicePaymentBase(Invoice invoice, NrveValue nrveAmount) {
        this.invoice = invoice;
        this.nrveAmount = nrveAmount;
    }

    public boolean hasBeenPaid() {
        return !isEmpty(getTransactionId()) && getTransactionDate() != null;
    }

    public abstract WalletTransactionStatus getInitialWalletTransactionStatus();

    public abstract WalletTransaction getPaymentWalletTransaction();
    public abstract void setPaymentWalletTransaction(WalletTransaction transaction);

    public abstract WalletTransaction getRefundWalletTransaction();
    public abstract void setRefundWalletTransaction(WalletTransaction transaction);

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
