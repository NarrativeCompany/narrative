package org.narrative.network.core.narrative.wallet;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.function.Function;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 10:36
 *
 * @author brian
 */
public enum NeoTransactionType implements IntegerEnum {
    // referral reward lump sum transfer. for now, created just once during a patch
    REFERRAL_REWARDS(17, NeoWalletType.NARRATIVE_COMPANY, NeoWalletType.MEMBER_CREDITS),

    // annual mint tokens transaction
    MINT_TOKENS(0, NeoWalletType.NRVE_SMART_CONTRACT, NeoWalletType.TOKEN_MINT)

    // transfers into prorated niche revenue
    ,NICHE_NRVE_REVENUE(1, NeoWalletType.NICHE_PAYMENT, NeoWalletType.PRORATED_NICHE_REVENUE)
    ,NICHE_FIAT_REVENUE(2, NeoWalletType.CHANNEL_FIAT_HOLDING, NeoWalletType.PRORATED_NICHE_REVENUE)

    // transfers from prorated niche revenue
    ,BULK_NICHE_REFUND(13, NeoWalletType.PRORATED_NICHE_REVENUE, NeoWalletType.MEMBER_CREDITS)
    ,NICHE_FIAT_PAYMENT_REVERSAL(15, NeoWalletType.PRORATED_NICHE_REVENUE, NeoWalletType.CHANNEL_FIAT_HOLDING)
    ,PRORATED_MONTH_NICHE_REVENUE(6, NeoWalletType.PRORATED_NICHE_REVENUE, NeoWalletType.MONTHLY_REWARDS)

    // transfers into prorated publication revenue
    ,PUBLICATION_NRVE_REVENUE(3, NeoWalletType.PUBLICATION_PAYMENT, NeoWalletType.PRORATED_PUBLICATION_REVENUE)
    ,PUBLICATION_FIAT_REVENUE(4, NeoWalletType.CHANNEL_FIAT_HOLDING, NeoWalletType.PRORATED_PUBLICATION_REVENUE)

    // transfers from prorated publication revenue
    ,BULK_PUBLICATION_REFUND(14, NeoWalletType.PRORATED_PUBLICATION_REVENUE, NeoWalletType.MEMBER_CREDITS)
    ,PUBLICATION_FIAT_PAYMENT_REVERSAL(16, NeoWalletType.PRORATED_PUBLICATION_REVENUE, NeoWalletType.CHANNEL_FIAT_HOLDING)
    ,PRORATED_MONTH_PUBLICATION_REVENUE(7, NeoWalletType.PRORATED_PUBLICATION_REVENUE, NeoWalletType.MONTHLY_REWARDS)

    // remaining revenue transfers to Monthly Rewards (niche and publication revenue handled above)
    ,TOKEN_MINT_REVENUE(5, NeoWalletType.TOKEN_MINT, NeoWalletType.MONTHLY_REWARDS)
    ,ADVERTISING_REVENUE(8, NeoWalletType.ADVERTISING_PAYMENT, NeoWalletType.MONTHLY_REWARDS)
    // bl: this includes deleted user abandoned NRVE revenue as well as refund reversals
    ,MISCELLANEOUS_REVENUE(9, NeoWalletType.MEMBER_CREDITS, NeoWalletType.MONTHLY_REWARDS)

    // actual revenue distribution to Narrative Company (15%) and members (85%)
    ,NARRATIVE_COMPANY_MONTH_REVENUE(10, NeoWalletType.MONTHLY_REWARDS, NeoWalletType.NARRATIVE_COMPANY)
    ,ALL_USERS_MONTH_CREDITS(11, NeoWalletType.MONTHLY_REWARDS, NeoWalletType.MEMBER_CREDITS)

    // bl: one bulk transfer for all redemptions in a batch goes to REDEMPTION_TEMP
    ,MEMBER_CREDITS_BULK_REDEMPTION(18, NeoWalletType.MEMBER_CREDITS, NeoWalletType.REDEMPTION_TEMP)
    // then each user gets their own individual redemption transaction
    ,MEMBER_CREDITS_REDEMPTION(12, NeoWalletType.REDEMPTION_TEMP, NeoWalletType.USER)
    ;

    private final int id;
    private final NeoWalletType fromWalletType;
    private final NeoWalletType toWalletType;

    NeoTransactionType(int id, NeoWalletType fromWalletType, NeoWalletType toWalletType) {
        this.id = id;
        this.fromWalletType = fromWalletType;
        this.toWalletType = toWalletType;
    }

    @Override
    public int getId() {
        return id;
    }

    public NeoWalletType getFromWalletType() {
        return fromWalletType;
    }

    public NeoWalletType getToWalletType() {
        return toWalletType;
    }

    public boolean isAllowsTransactionManagement() {
        // bl: don't allow MEMBER_CREDITS_REDEMPTION transactions to be managed on the NEO Transactions page
        return !isMemberCreditsRedemption();
    }

    public boolean isValidTransaction(NeoWallet fromNeoWallet, NeoWallet toNeoWallet) {
        return isEqual(fromWalletType, fromNeoWallet.getType()) && isEqual(toWalletType, toNeoWallet.getType());
    }

    public WalletTransactionType getWalletTransactionType() {
        return WalletTransactionType.NEO_TRANSACTION_TYPE_TO_WALLET_TRANSACTION_TYPE.get(this);
    }

    public NeoWallet getFromNeoWallet(WalletTransaction walletTransaction) {
        return getNeoWalletResolved(fromWalletType, walletTransaction, WalletTransaction::getFromWallet, "from");
    }

    public NeoWallet getToNeoWallet(WalletTransaction walletTransaction) {
        return getNeoWalletResolved(toWalletType, walletTransaction, WalletTransaction::getToWallet, "to");
    }

    private static NeoWallet getNeoWalletResolved(NeoWalletType neoWalletType, WalletTransaction walletTransaction, Function<WalletTransaction, Wallet> getNeoWallet, String debugWalletType) {
        // bl: singleton wallets are easy
        if(neoWalletType.isSingleton()) {
            return NeoWallet.dao().getSingletonWallet(neoWalletType);
        }
        // otherwise, we must be able to get a wallet from the transaction!
        Wallet wallet = getNeoWallet.apply(walletTransaction);
        assert exists(wallet) : "Should always have a " + debugWalletType + "Wallet! transaction/" + walletTransaction.getOid() + " type/" + walletTransaction.getType();
        NeoWallet neoWallet = wallet.getNeoWallet();
        assert exists(neoWallet) : "Should always get a " + debugWalletType + "NeoWallet! transaction/" + walletTransaction.getOid() + " type/" + walletTransaction.getType() + " " + debugWalletType + "Wallet/" + wallet.getOid();
        assert neoWalletType==neoWallet.getType() : "Type mismatch for " + debugWalletType + "NeoWallet! Expected " + neoWalletType + " but got " + neoWallet.getType();
        return neoWallet;
    }

    public boolean isMemberCreditsRedemption() {
        return this == MEMBER_CREDITS_REDEMPTION;
    }
}
