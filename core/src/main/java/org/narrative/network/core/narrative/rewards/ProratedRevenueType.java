package org.narrative.network.core.narrative.rewards;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-16
 * Time: 15:28
 *
 * @author jonmark
 */
public enum ProratedRevenueType implements IntegerEnum {
    NICHE_REVENUE(0, WalletType.NICHE_MONTH_REVENUE, NeoWalletType.NICHE_PAYMENT, WalletTransactionType.PRORATED_NICHE_MONTH_REVENUE, WalletTransactionType.NICHE_PAYMENT, WalletTransactionType.NICHE_REFUND, NeoTransactionType.NICHE_NRVE_REVENUE, NeoTransactionType.NICHE_FIAT_REVENUE, NeoTransactionType.BULK_NICHE_REFUND)
    ,PUBLICATION_REVENUE(1, WalletType.PUBLICATION_MONTH_REVENUE, NeoWalletType.PUBLICATION_PAYMENT, WalletTransactionType.PRORATED_PUBLICATION_MONTH_REVENUE, WalletTransactionType.PUBLICATION_PAYMENT, WalletTransactionType.PUBLICATION_REFUND, NeoTransactionType.PUBLICATION_NRVE_REVENUE, NeoTransactionType.PUBLICATION_FIAT_REVENUE, NeoTransactionType.BULK_PUBLICATION_REFUND)
    ;

    public static final Set<ProratedRevenueType> ACTIVE_TYPES = Collections.unmodifiableSet(EnumSet.allOf(ProratedRevenueType.class));
    public static final Map<WalletType, ProratedRevenueType> WALLET_TYPE_TO_PRORATED_REVENUE_TYPE = Collections.unmodifiableMap(EnumSet.allOf(ProratedRevenueType.class).stream().collect(Collectors.toMap(ProratedRevenueType::getWalletType, Function.identity())));

    private final int id;
    private final WalletType walletType;
    private final NeoWalletType nrvePaymentNeoWalletType;
    private final WalletTransactionType revenueTransactionType;
    private final WalletTransactionType paymentTransactionType;
    private final WalletTransactionType refundTransactionType;
    private final NeoTransactionType nrveRevenueNeoTransactionType;
    private final NeoTransactionType fiatRevenueNeoTransactionType;
    private final NeoTransactionType bulkRefundNeoTransactionType;

    ProratedRevenueType(int id, WalletType walletType, NeoWalletType nrvePaymentNeoWalletType, WalletTransactionType revenueTransactionType, WalletTransactionType paymentTransactionType, WalletTransactionType refundTransactionType, NeoTransactionType nrveRevenueNeoTransactionType, NeoTransactionType fiatRevenueNeoTransactionType, NeoTransactionType bulkRefundNeoTransactionType) {
        assert walletType.equals(revenueTransactionType.getFromWalletType()) : "Revenue should come from the revenue wallet! type/" + this;
        assert walletType.equals(paymentTransactionType.getToWalletType()) : "Payments should go to the revenue wallet! type/" + this;
        assert walletType.equals(refundTransactionType.getFromWalletType()) : "Refunds should come from the revenue wallet! type/" + this;
        assert WalletType.USER.equals(refundTransactionType.getToWalletType()) : "Refunds should always go to user wallets! type/" + this;
        assert walletType.getNeoWalletType().equals(bulkRefundNeoTransactionType.getFromWalletType()) : "Bulk refunds should always come from the revenue's NeoWallet! type/" + this;
        assert NeoWalletType.MEMBER_CREDITS.equals(bulkRefundNeoTransactionType.getToWalletType()) : "Bulk refunds should always go to the Member Credits NeoWallet! type/" + this;
        this.id = id;
        this.walletType = walletType;
        this.nrvePaymentNeoWalletType = nrvePaymentNeoWalletType;
        this.revenueTransactionType = revenueTransactionType;
        this.paymentTransactionType = paymentTransactionType;
        this.refundTransactionType = refundTransactionType;
        this.bulkRefundNeoTransactionType = bulkRefundNeoTransactionType;
        this.nrveRevenueNeoTransactionType = nrveRevenueNeoTransactionType;
        this.fiatRevenueNeoTransactionType = fiatRevenueNeoTransactionType;
    }

    @Override
    public int getId() {
        return id;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public NeoWalletType getNrvePaymentNeoWalletType() {
        return nrvePaymentNeoWalletType;
    }

    public WalletTransactionType getRevenueTransactionType() {
        return revenueTransactionType;
    }

    public WalletTransactionType getPaymentTransactionType() {
        return paymentTransactionType;
    }

    public WalletTransactionType getRefundTransactionType() {
        return refundTransactionType;
    }

    public NeoTransactionType getNrveRevenueNeoTransactionType() {
        return nrveRevenueNeoTransactionType;
    }

    public NeoTransactionType getFiatRevenueNeoTransactionType() {
        return fiatRevenueNeoTransactionType;
    }

    public NeoTransactionType getBulkRefundNeoTransactionType() {
        return bulkRefundNeoTransactionType;
    }
}