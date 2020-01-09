package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.dao.NeoWalletDAO;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Month;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 10:35
 *
 * @author brian
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uidx_neoWallet_type", columnNames = {NeoWallet.FIELD__TYPE__COLUMN, NeoWallet.FIELD__SINGLETON__COLUMN}),
        @UniqueConstraint(name = "uidx_neoWallet_neoAddress", columnNames = {NeoWallet.FIELD__NEO_ADDRESS__COLUMN})
})
public class NeoWallet implements DAOObject<NeoWalletDAO> {
    private static final String FIELD__TYPE__NAME = "type";
    static final String FIELD__TYPE__COLUMN = FIELD__TYPE__NAME;
    private static final String FIELD__SINGLETON__NAME = "singleton";
    static final String FIELD__SINGLETON__COLUMN = FIELD__SINGLETON__NAME;
    private static final String FIELD__NEO_ADDRESS__NAME = "neoAddress";
    static final String FIELD__NEO_ADDRESS__COLUMN = FIELD__NEO_ADDRESS__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private NeoWalletType type;

    private Boolean singleton;

    private String neoAddress;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient Wallet wallet;

    public NeoWallet(NeoWalletType type) {
        this.type = type;
        // bl: the singleton flag is used to enforce uniqueness by wallet type. for types that are not singletons,
        // we use a null value so that uniqueness is not enforced
        this.singleton = type.isSingleton() ? Boolean.TRUE : null;
        if(type.isSingleton() && NetworkRegistry.getInstance().isProductionServer()) {
            this.neoAddress = type.getProductionDefaultNeoAddress();
        }
    }

    public String getNameForDisplay() {
        String name = getType().getNameForDisplay();
        if(getType().isProratedMonthRevenue()) {
            return name + ": " + getProratedMonthRevenue().getRewardYearMonth().getFormatted();
        }
        return name;
    }

    public Wallet getWallet() {
        assert !getType().isUser() : "This method should never be used for user wallets, where multiple users could be associated to the same NeoWallet.";

        if(wallet==null) {
            wallet = Wallet.dao().getUniqueBy(new NameValuePair<>(Wallet.Fields.neoWallet, this));
        }
        return wallet;
    }

    public ProratedMonthRevenue getProratedMonthRevenue() {
        assert getType().isProratedMonthRevenue() : "Should not get ProratedMonthRevenue for types that don't support it! type/" + getType();
        return getWallet().getProratedMonthRevenue();
    }

    public NrveValue getExpectedBalance() {
        ProratedRevenueType nrveRevenueType = getType().getNrvePaymentWalletForProratedRevenueType();
        Collection<ProratedRevenueType> fiatRevenueTypes = getType().getFiatPaymentWalletForProratedRevenueTypes();
        // bl: if this is a ProratedMonthRevenue payment wallet, then let's derive the expected balance based
        // on all of the transactions to date in all incomplete RewardPeriods for ProratedMonthRevenue
        if(nrveRevenueType!=null || !isEmptyOrNull(fiatRevenueTypes)) {
            List<RewardPeriod> incompleteRewardPeriods = RewardPeriod.dao().getAllIncompleteRewardPeriods();
            // bl: now get all of the ProratedMonthRevenue Wallets for those periods
            List<YearMonth> incompleteYearMonths = incompleteRewardPeriods.stream().map(RewardPeriod::getPeriod).collect(Collectors.toList());
            Collection<ProratedRevenueType> proratedRevenueTypes = nrveRevenueType!=null ? Collections.singleton(nrveRevenueType) : fiatRevenueTypes;
            List<Wallet> wallets = ProratedMonthRevenue.dao().getWalletsForTypeAndYearMonths(proratedRevenueTypes, incompleteYearMonths);
            // bl: just like ApplyFiatAdjustmentTask, sum up all of the transactions for these wallets.
            // bl: NRVE payments should be completed, and fiat payments should be pending.
            return WalletTransaction.dao().getSumForToWalletsAndStatus(wallets, nrveRevenueType!=null ? WalletTransactionStatus.COMPLETED : WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT);
        }
        // bl: for Monthly Rewards, we always expect the balance to be zero, but there may be some carryover from
        // the last month's rewards, so look for that
        if(getType().isMonthlyRewards()) {
            // bl: just get the oldest incomplete RewardPeriod and get its wallet balance. since the query user a before date,
            // just use a value far in the future.
            RewardPeriod rewardPeriod = RewardPeriod.dao().getOldestIncompleteRewardPeriodBefore(YearMonth.of(3000, Month.JANUARY));
            return exists(rewardPeriod) ? rewardPeriod.getWallet().getBalance() : NrveValue.ZERO;
        }
        // bl: the member credits wallet balance is equal to the sum of all user wallet balances.
        // bl: PLUS the sum of all pending redemptions.
        if(getType().isMemberCredits()) {
            NrveValue userWalletSum = Wallet.dao().getSumOfAllBalancesForType(WalletType.USER);
            NrveValue pendingRedemptionSum = WalletTransaction.dao().getTransactionSumByTypeWithStatus(WalletTransactionType.USER_REDEMPTION, WalletTransactionStatus.PENDING);
            return userWalletSum.add(pendingRedemptionSum);
        }
        // bl: for ProratedMonthRevenue, the expected balance is 0 for all months that haven't had rewards processed yet
        if(getType().isProratedMonthRevenue()) {
            ProratedMonthRevenue proratedMonthRevenue = getProratedMonthRevenue();
            RewardPeriod rewardPeriod = RewardPeriod.dao().getForYearMonth(proratedMonthRevenue.getMonth());
            // bl: if rewards aren't completed yet, then the balance is expected to be zero. otherwise, if rewards
            // have been completed, then we expect the balance to be the wallet's balance (handled below).
            // bl: there is ProratedMonthRevenue for April which doesn't have a corresponding RewardPeriod.
            if(exists(rewardPeriod) && !rewardPeriod.isCompleted()) {
                // bl: return null so it doesn't show the value since we really shouldn't need to audit
                // future ProratedMonthRevenue wallets
                return null;
            }
        }
        // bl: if there is a wallet, then use its balance
        if(exists(getWallet())) {
            return getWallet().getBalance();
        }

        // bl: otherwise, there is no balance to show
        return null;
    }

    public boolean isActive() {
        // bl: first, handle any types that are not active
        if(!getType().isActive()) {
            return false;
        }
        // bl: it's not active if it doesn't have an address set yet
        if(isEmpty(getNeoAddress())) {
            return false;
        }
        // bl: now, ignore any ProratedMonthRevenue values in the future
        if(getType().isProratedMonthRevenue()) {
            YearMonth revenueMonth = getProratedMonthRevenue().getMonth();
            // bl: it's active if the revenue month is not in the future
            // bl: also drop off old revenue wallets once they are 13 months old. since we will have wallets for
            // both Niches and Publications, this ensures we'll have at most 26 wallets displayed for prorated revenue.
            return !revenueMonth.isAfter(RewardUtils.nowYearMonth()) && !revenueMonth.isBefore(RewardUtils.nowYearMonth().minusMonths(13));
        }
        // bl: if all of the above tests passed, then it's active!
        return true;
    }

    public String getScriptHash() {
        // bl: only include the script hash for the smart contract NeoWallet
        if(!getType().isNrveSmartContract()) {
            return null;
        }
        return areaContext().getAreaRlm().getSandboxedCommunitySettings().getNrveScriptHash();
    }

    public String getExtraNeoAddress() {
        // bl: only include an extra NEO address for the Narrative Company NeoWallet.
        // note that we have NRVE distributed across these two wallets which is why this is necessary.
        if(!getType().isNarrativeCompany()) {
            return null;
        }
        return areaContext().getAreaRlm().getSandboxedCommunitySettings().getExtraNarrativeCompanyNeoWalletAddress();
    }

    public String getMonthForDisplay() {
        // bl: only include the month for display for ProratedMonthRevenue wallets
        if(!getType().isProratedMonthRevenue()) {
            return null;
        }
        return getProratedMonthRevenue().getRewardYearMonth().getFormatted();
    }

    /**
     * sort NeoWallets by the order in the enum, and then by the ProratedMonthRevenue month
     */
    public static final Comparator<NeoWallet> COMPARATOR = (o1, o2) -> {
        int ret = o1.getType().compareTo(o2.getType());
        if(ret!=0) {
            return ret;
        }
        // bl: if it's singleton, there should be only 1 instance, so return 0
        if(o1.getType().isSingleton()) {
            assert o1==o2 : "Should never have two instances of singleton NeoWallets!";
            return 0;
        }
        // bl: last but not least, let's sort by the ProratedMonthRevenue month
        if(o1.getType().isProratedMonthRevenue()) {
            return o1.getProratedMonthRevenue().getMonth().compareTo(o2.getProratedMonthRevenue().getMonth());
        }
        // bl: otherwise, it might be a Redemption Temp wallet or some other non-singleton. no special ordering for those
        return 0;
    };

    public static NeoWalletDAO dao() {
        return NetworkDAOImpl.getDAO(NeoWallet.class);
    }
}
