package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/4/18
 * Time: 11:35 AM
 */
public enum WalletTransactionType implements IntegerEnum {
    // jw: initial Referral Program Types
    REFERRAL(0, null, null, WalletType.USER)
    ,REFERRAL_TOP_10(1, null, null, WalletType.USER)

    // jw: PRORATED_MONTH_REVENUE adjustments
    ,NICHE_PAYMENT(2, null, null, WalletType.NICHE_MONTH_REVENUE)
    ,NICHE_REFUND(3, null, WalletType.NICHE_MONTH_REVENUE, WalletType.USER)
    ,NICHE_FIAT_PAYMENT_REVERSAL(21, NeoTransactionType.NICHE_FIAT_PAYMENT_REVERSAL, WalletType.NICHE_MONTH_REVENUE, null)
    ,PUBLICATION_PAYMENT(4, null, null, WalletType.PUBLICATION_MONTH_REVENUE)
    ,PUBLICATION_REFUND(5, null, WalletType.PUBLICATION_MONTH_REVENUE, WalletType.USER)
    ,PUBLICATION_FIAT_PAYMENT_REVERSAL(23, NeoTransactionType.PUBLICATION_FIAT_PAYMENT_REVERSAL, WalletType.PUBLICATION_MONTH_REVENUE, null)

    // jw: REWARD_PERIOD adjustments
    ,MINTED_TOKENS(6, NeoTransactionType.TOKEN_MINT_REVENUE, WalletType.TOKEN_MINT, WalletType.REWARD_PERIOD)
    ,PRORATED_NICHE_MONTH_REVENUE(7, NeoTransactionType.PRORATED_MONTH_NICHE_REVENUE, WalletType.NICHE_MONTH_REVENUE, WalletType.REWARD_PERIOD)
    ,PRORATED_PUBLICATION_MONTH_REVENUE(8, NeoTransactionType.PRORATED_MONTH_PUBLICATION_REVENUE, WalletType.PUBLICATION_MONTH_REVENUE, WalletType.REWARD_PERIOD)
    ,ADVERTISING_REVENUE(9, NeoTransactionType.ADVERTISING_REVENUE, null, WalletType.REWARD_PERIOD)
    ,DELETED_USER_ABANDONED_BALANCES(10, null, WalletType.USER, WalletType.REWARD_PERIOD)
    ,REFUND_REVERSAL(22, null, WalletType.USER, WalletType.REWARD_PERIOD)

    // jw: REWARD_PERIOD -> REWARD_PERIOD
    ,REWARD_PERIOD_CARRYOVER(11, null, WalletType.REWARD_PERIOD, WalletType.REWARD_PERIOD)

    // jw: REWARD_PERIOD -> USER transactions
    ,CONTENT_REWARD(12, null, WalletType.REWARD_PERIOD, WalletType.USER)
    ,NICHE_OWNERSHIP_REWARD(13, null, WalletType.REWARD_PERIOD, WalletType.USER)
    ,NICHE_MODERATION_REWARD(14, null, WalletType.REWARD_PERIOD, WalletType.USER)
    ,ACTIVITY_REWARD(15, null, WalletType.REWARD_PERIOD, WalletType.USER)
    ,TRIBUNAL_REWARD(16, null, WalletType.REWARD_PERIOD, WalletType.USER)
    ,NARRATIVE_COMPANY_REWARD(17, NeoTransactionType.NARRATIVE_COMPANY_MONTH_REVENUE, WalletType.REWARD_PERIOD, null)
    ,ELECTORATE_REWARD(18, null, WalletType.REWARD_PERIOD, WalletType.USER)

    // jw: USER -> USER transactions
    ,USER_TIP(19, null, WalletType.USER, WalletType.USER)

    // bl: USER -> null transactions
    // bl: can't link this to NeoTransactionType.MEMBER_CREDITS_REDEMPTION since the NeoTransaction needs to be created
    // through a separate process, so it can't be created at the time the WalletTransaction is created.
    ,USER_REDEMPTION(24, null, WalletType.USER, null)

    // jw: null -> TOKEN_MINT transactions
    ,INITIAL_TOKEN_MINT(20, NeoTransactionType.MINT_TOKENS, null, WalletType.TOKEN_MINT)
    ;

    public static final Set<WalletTransactionType> REFERRAL_TYPES = Collections.unmodifiableSet(EnumSet.of(REFERRAL, REFERRAL_TOP_10));
    public static final Set<WalletTransactionType> PRIVATE_USER_TYPES = Collections.unmodifiableSet(EnumSet.of(USER_TIP, USER_REDEMPTION));
    /**
     * all of the types that have metadata to associate with the {@link WalletTransaction}
     */
    public static final Set<WalletTransactionType> METADATA_TYPES = Collections.unmodifiableSet(EnumSet.of(NICHE_REFUND, PUBLICATION_REFUND, REFUND_REVERSAL, CONTENT_REWARD, NICHE_OWNERSHIP_REWARD, NICHE_MODERATION_REWARD, ACTIVITY_REWARD, USER_TIP));
    public static final Map<NeoTransactionType,WalletTransactionType> NEO_TRANSACTION_TYPE_TO_WALLET_TRANSACTION_TYPE = Collections.unmodifiableMap(EnumSet.allOf(WalletTransactionType.class).stream().filter(t -> t.getNeoTransactionType()!=null).collect(Collectors.toMap(WalletTransactionType::getNeoTransactionType, Function.identity())));
    public static final Set<WalletTransactionType> REVENUE_TYPES;

    static {
        Set<WalletTransactionType> revenueTypes = EnumSet.noneOf(WalletTransactionType.class);
        for (WalletTransactionType type : values()) {
            if(type.getSupportedToWalletTypes().contains(WalletType.REWARD_PERIOD)) {
                revenueTypes.add(type);
            }
        }
        REVENUE_TYPES = Collections.unmodifiableSet(revenueTypes);
    }

    private final int id;
    private final NeoTransactionType neoTransactionType;
    // jw: stores the from and to wallets that can be used for this transaction.
    private final Set<ObjectPair<WalletType, WalletType>> supportedTransactionProfiles;

    WalletTransactionType(int id, NeoTransactionType neoTransactionType, WalletType from, WalletType to) {
        this(id, neoTransactionType, Collections.singleton(new ObjectPair<>(from, to)));
        assert from!=null || to!=null : "Must specify at least one wallet type!";
    }

    WalletTransactionType(int id, NeoTransactionType neoTransactionType, Set<ObjectPair<WalletType, WalletType>> supportedTransactionProfiles) {
        assert !isEmptyOrNull(supportedTransactionProfiles) : "There should always be at least one transaction profile specified.";

        this.id = id;
        this.neoTransactionType = neoTransactionType;
        this.supportedTransactionProfiles = Collections.unmodifiableSet(supportedTransactionProfiles);
    }

    public boolean isValidTransaction(Wallet fromWallet, Wallet toWallet) {
        return isValidTransaction(
                exists(fromWallet) ? fromWallet.getType() : null,
                exists(toWallet) ? toWallet.getType() : null
        );
    }

    public boolean isValidTransaction(WalletType fromWalletType, WalletType toWalletType) {
        ObjectPair<WalletType, WalletType> profile = new ObjectPair<>(
                fromWalletType,
                toWalletType
        );

        return supportedTransactionProfiles.contains(profile);
    }

    public Set<WalletType> getSupportedFromWalletTypes() {
        return ObjectPair.getAllUniqueOnes(supportedTransactionProfiles);
    }

    public Set<WalletType> getSupportedToWalletTypes() {
        return ObjectPair.getAllUniqueTwos(supportedTransactionProfiles);
    }

    public WalletType getFromWalletType() {
        assert supportedTransactionProfiles.size()==1 : "Should never use this method for types that support multiple profiles! type/" + this;
        return supportedTransactionProfiles.iterator().next().getOne();
    }

    public WalletType getToWalletType() {
        assert supportedTransactionProfiles.size()==1 : "Should never use this method for types that support multiple profiles! type/" + this;
        return supportedTransactionProfiles.iterator().next().getTwo();
    }

    public Set<WalletTransactionStatus> getSupportedStatuses() {
        // jw: for user redemptions we support
        if (isUserRedemption()) {
            return WalletTransactionStatus.BULK_PROCESSED_STATUSES;
        }

        // bl: due to static initialization order issues, can't do this initialization in the constructor.
        // ProratedRevenueType depends on WalletType and WalletTransactionType, so both of these enums
        // have to be initialized before ProratedRevenueType's static lookup map is initialized.
        for (WalletType toWalletType : getSupportedToWalletTypes()) {
            if(toWalletType!=null && toWalletType.isProratedMonthRevenue()) {
                return WalletTransactionStatus.FIAT_ADJUSTMENT_STATUSES;
            }
        }

        // jw: most types only support completed status.
        return WalletTransactionStatus.COMPLETED_STATUS_ONLY;
    }

    public boolean isMiscellaneousRevenue() {
        WalletType fromWalletType = getFromWalletType();
        WalletType toWalletType = getToWalletType();
        // bl: miscellaneous revenue is anything transferred from users to the reward period
        boolean ret = fromWalletType!=null && fromWalletType.isUser() && toWalletType!=null && toWalletType.isRewardPeriod();

        // bl: right now, there are only two types that we expect for miscellaneous revenue. if we add more,
        // we'll need to update UIs, so adding an extra enforcement here
        assert ret == (isDeletedUserAbandonedBalances() || isRefundReversal()) : "Found an unexpected value for miscellaenousRevenue! type/" + this;

        return ret;
    }

    @Override
    public int getId() {
        return id;
    }

    public NeoTransactionType getNeoTransactionType() {
        return neoTransactionType;
    }

    public boolean isUserToUserTransaction() {
        return isUserTip();
    }

    public boolean isProratedMonthRevenue() {
        return getFromWalletType().isProratedMonthRevenue();
    }

    public boolean isMintedTokens() {
        return this == MINTED_TOKENS;
    }

    public boolean isDeletedUserAbandonedBalances() {
        return this == DELETED_USER_ABANDONED_BALANCES;
    }

    public boolean isRefundReversal() {
        return this == REFUND_REVERSAL;
    }

    public boolean isRewardPeriodCarryover() {
        return this == REWARD_PERIOD_CARRYOVER;
    }

    public boolean isContentReward() {
        return this == CONTENT_REWARD;
    }

    public boolean isNicheOwnershipReward() {
        return this == NICHE_OWNERSHIP_REWARD;
    }

    public boolean isNicheModerationReward() {
        return this == NICHE_MODERATION_REWARD;
    }

    public boolean isActivityReward() {
        return this == ACTIVITY_REWARD;
    }

    public boolean isTribunalReward() {
        return this == TRIBUNAL_REWARD;
    }

    public boolean isNarrativeCompanyReward() {
        return this == NARRATIVE_COMPANY_REWARD;
    }

    public boolean isElectorateReward() {
        return this == ELECTORATE_REWARD;
    }

    public boolean isUserTip() {
        return this == USER_TIP;
    }

    public boolean isUserRedemption() {
        return this == USER_REDEMPTION;
    }
}