package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.HibernateYearMonthType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.dao.ProratedMonthRevenueDAO;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletRef;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.YearMonth;
import java.util.Map;

/**
 * Date: 2019-05-16
 * Time: 15:22
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {
        // jw: content should only be awarded once within a period
        @UniqueConstraint(name = "proratedMonthRevenue_type_month_uidx", columnNames = {ProratedMonthRevenue.FIELD__TYPE__COLUMN, ProratedMonthRevenue.FIELD__MONTH__COLUMN})
})
public class ProratedMonthRevenue implements DAOObject<ProratedMonthRevenueDAO>, WalletRef {
    public static final String FIELD__TYPE__NAME = "type";
    public static final String FIELD__TYPE__COLUMN = FIELD__TYPE__NAME;

    public static final String FIELD__MONTH__NAME = "month";
    public static final String FIELD__MONTH__COLUMN = FIELD__MONTH__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private ProratedRevenueType type;

    @NotNull
    @Type(type = HibernateYearMonthType.TYPE)
    private YearMonth month;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_proratedMonthRevenue_wallet")
    private Wallet wallet;

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue totalNrve;

    private int captures;

    @Transient
    private transient RewardYearMonth rewardYearMonth;

    public ProratedMonthRevenue(ProratedRevenueType type, YearMonth month) {
        this.type = type;
        this.month = month;

        // jw: Create the wallet for this ProratedMonthRevenue so that it will get saved along with this object.
        wallet = new Wallet(type.getWalletType());

        // jw: prime this to 0
        totalNrve = NrveValue.ZERO;
        captures = 0;
    }

    // jw: This utility method is responsible for managing the state of this revenue object.
    public NrveValue calculateCurrentCaptureValue() {
        if (getCaptures() > RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR) {
            throw UnexpectedError.getRuntimeException("Someone is attempting to capture beyond the life of a ProratedMonthRevenue/"+getOid());
        }
        if (getCaptures() < 1) {
            throw UnexpectedError.getRuntimeException("Someone is attempting to capture before increment capture count a ProratedMonthRevenue/"+getOid());
        }

        // jw: now the easy part, calculate the capture and send it back.
        return RewardUtils.calculateCaptureValue(getTotalNrve(), getCaptures(), RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR);
    }

    public RewardYearMonth getRewardYearMonth() {
        if(rewardYearMonth==null) {
            // bl: April has a ProratedMonthRevenue, so it's the first active YearMonth
            rewardYearMonth = new RewardYearMonth(getMonth(), RewardUtils.APRIL_2019);
        }
        return rewardYearMonth;
    }

    public NeoTransaction recordFiatPaymentNeoTransaction() {
        NrveValue fiatPaymentTotal = FiatPayment.dao().getTransactionSumToWallet(getWallet());
        NeoWallet fromNeoWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.CHANNEL_FIAT_HOLDING);
        NeoTransaction neoTransaction = new NeoTransaction(getType().getFiatRevenueNeoTransactionType(), fromNeoWallet, getWallet().getNeoWallet(), fiatPaymentTotal);
        NeoTransaction.dao().save(neoTransaction);
        return neoTransaction;
    }

    public NeoTransaction recordNrvePaymentNeoTransaction() {
        NrveValue nrvePaymentTotal = NrvePayment.dao().getTransactionSumToWallet(getWallet());
        NeoWallet fromNeoWallet = NeoWallet.dao().getSingletonWallet(getType().getNrvePaymentNeoWalletType());
        NeoTransaction neoTransaction = new NeoTransaction(getType().getNrveRevenueNeoTransactionType(), fromNeoWallet, getWallet().getNeoWallet(), nrvePaymentTotal);
        NeoTransaction.dao().save(neoTransaction);
        return neoTransaction;
    }

    public void recordBulkRefundsNeoTransaction() {
        NeoTransactionType neoTransactionType = getType().getBulkRefundNeoTransactionType();
        NeoWallet toWallet = NeoWallet.dao().getSingletonWallet(neoTransactionType.getToWalletType());
        Map<Wallet,NrveValue> walletToRefundTotal = WalletTransaction.dao().getRefundTransactionSumsByFromWalletInRange(this);
        for (Map.Entry<Wallet, NrveValue> entry : walletToRefundTotal.entrySet()) {
            Wallet proratedMonthRevenueWallet = entry.getKey();
            NrveValue totalRefundAmount = entry.getValue();
            if(NrveValue.ZERO.equals(totalRefundAmount)) {
                continue;
            }
            NeoTransaction neoTransaction = new NeoTransaction(neoTransactionType, proratedMonthRevenueWallet.getNeoWallet(), toWallet, totalRefundAmount);
            NeoTransaction.dao().save(neoTransaction);
        }
    }

    public static ProratedMonthRevenueDAO dao() {
        return NetworkDAOImpl.getDAO(ProratedMonthRevenue.class);
    }
}
