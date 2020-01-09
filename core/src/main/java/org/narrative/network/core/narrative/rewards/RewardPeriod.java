package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateEnumSetType;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.HibernateYearMonthType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.dao.RewardPeriodDAO;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletRef;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-13
 * Time: 13:55
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "rewardPeriod_period_uidx", columnNames = {RewardPeriod.FIELD__PERIOD__COLUMN})
})
public class RewardPeriod implements DAOObject<RewardPeriodDAO>, WalletRef {
    public static final String FIELD__PERIOD__NAME = "period";
    public static final String FIELD__PERIOD__COLUMN = FIELD__PERIOD__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = HibernateYearMonthType.TYPE)
    private YearMonth period;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_rewardPeriod_wallet")
    private Wallet wallet;

    @Type(type = IntegerEnumType.TYPE)
    private TokenMintYear mintYear;

    @Column(columnDefinition = "tinyint")
    private Integer mintMonth;

    @NotNull
    @Type(type = HibernateEnumSetType.TYPE, parameters = {@Parameter(name = HibernateEnumSetType.ENUM_CLASS, value = RewardPeriodStep.TYPE)})
    private EnumSet<RewardPeriodStep> completedSteps;

    @Type(type = HibernateInstantType.TYPE)
    private Instant completedDatetime;

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue totalRewards;
    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue totalRewardsDisbursed;

    @Transient
    private transient RewardYearMonth rewardYearMonth;

    public RewardPeriod(YearMonth period, TokenMintYear mintYear, Integer mintMonth) {
        assert period != null : "Should always have a period to define the month for this RewardPeriod.";
        assert period.compareTo(RewardUtils.FIRST_ACTIVE_YEAR_MONTH) >= 0 : "Should never create a reward period for before May 2019";
        assert (mintYear==null) == (mintMonth==null) : "RewardPeriods should always be created with both or neither of mintYear and mintMonth. Never one without the other.";
        assert mintMonth == null || (mintMonth > 0 && mintMonth <= 12) : "mintMonth must be between 1 and 12 if specified.";

        this.period = period;

        // jw: Create the wallet for this RewardPeriod so that it will get saved along with this object.
        this.wallet = new Wallet(WalletType.REWARD_PERIOD);

        this.mintYear = mintYear;
        this.mintMonth = mintMonth;

        // jw: let's just define these as empty objects so that we never have to do Null checks on them.
        this.completedSteps = EnumSet.noneOf(RewardPeriodStep.class);
        this.totalRewards = NrveValue.ZERO;
        this.totalRewardsDisbursed = NrveValue.ZERO;
    }

    public TokenMintYear getNextMintYear() {
        if (getMintYear() == null) {
            return null;
        }

        assert getMintMonth() != null : "We should always have a mintMonth if we have a mintYear.";

        // jw: if this is the last month for this year return the next year
        if (getMintMonth() == RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR) {
            return getMintYear().getNextYear();
        }

        // jw: guess we have more months left, so use the same year.
        return getMintYear();
    }

    public Integer getNextMintMonth() {
        // jw: this is super simple... Get the next year, and if we get it then we know we have a month to provide
        TokenMintYear nextYear = getNextMintYear();

        if (nextYear == null) {
            return null;
        }

        // jw: now this is easy, if the current month is the max then start back at one.
        if (getMintMonth() == RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR) {
            return 1;
        }

        // guess we still have more draws this year, so increment the current month by one.
        return getMintMonth() + 1;
    }

    public boolean isCompleted() {
        // jw: we will be marking the RewardPeriod as complete by setting the completedDatetime field, so that makes it easy to check.
        return getCompletedDatetime()!=null;
    }

    public boolean isEligibleForRewardProcessing() {
        return !isCompleted() && getRewardYearMonth().isBeforeNow();
    }

    public RewardYearMonth getRewardYearMonth() {
        if(rewardYearMonth==null) {
            rewardYearMonth = new RewardYearMonth(getPeriod(), RewardUtils.FIRST_ACTIVE_YEAR_MONTH);
        }
        return rewardYearMonth;
    }

    public String getFormatted() {
        String formatted = getRewardYearMonth().getFormatted();
        // bl: special handling for May 2019, which includes April. will return: April/May 2019
        if(RewardUtils.FIRST_ACTIVE_YEAR_MONTH.equals(getPeriod())) {
            return RewardUtils.APRIL_2019.format(RewardYearMonth.MONTH_NAME_FORMATTER) + "/" + formatted;
        }
        return formatted;
    }

    public NeoTransaction recordMiscellaneousRevenueNeoTransaction() {
        NeoWallet fromNeoWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.MEMBER_CREDITS);
        NeoWallet toNeoWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.MONTHLY_REWARDS);
        // bl: get all of the types matching our criteria (from user wallets to the monthly rewards wallet)
        Set<WalletTransactionType> miscellaneousTypes = EnumSet.allOf(WalletTransactionType.class).stream().filter(WalletTransactionType::isMiscellaneousRevenue).collect(Collectors.toSet());
        NrveValue amount = WalletTransaction.dao().getTransactionSumToWallet(getWallet(), miscellaneousTypes);
        // bl: only record the transfer if there is actually an amount to transfer. otherwise, skip it!
        if(amount.equals(NrveValue.ZERO)) {
            return null;
        }
        NeoTransaction neoTransaction = new NeoTransaction(NeoTransactionType.MISCELLANEOUS_REVENUE, fromNeoWallet, toNeoWallet, amount);
        NeoTransaction.dao().save(neoTransaction);
        return neoTransaction;
    }

    public void recordAllUsersMonthCreditsNeoTransaction() {
        NeoWallet fromNeoWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.MONTHLY_REWARDS);
        NeoWallet toNeoWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.MEMBER_CREDITS);
        WalletTransaction narrativeCompanyTransaction = WalletTransaction.dao().getForFromWalletType(getWallet(), WalletTransactionType.NARRATIVE_COMPANY_REWARD);
        // bl: the transfer amount is the total disbursed less the NARRATIVE_COMPANY amount
        NrveValue amount = getTotalRewardsDisbursed().subtract(narrativeCompanyTransaction.getNrveAmount());
        // bl: verify that the opposite is true (all transactions other than Narrative Company Reward AND, importantly, any monthly carryover that should remain in the wallet)
        NrveValue expectedAmount = WalletTransaction.dao().getTransactionSumFromWallet(getWallet(), EnumSet.complementOf(EnumSet.of(WalletTransactionType.NARRATIVE_COMPANY_REWARD, WalletTransactionType.REWARD_PERIOD_CARRYOVER)));
        if(!amount.equals(expectedAmount)) {
            throw UnexpectedError.getRuntimeException("Found an incorrect total for the Member Credits transfer! expected/" + expectedAmount + " actual/" + amount);
        }
        NeoTransaction neoTransaction = new NeoTransaction(NeoTransactionType.ALL_USERS_MONTH_CREDITS, fromNeoWallet, toNeoWallet, amount);
        NeoTransaction.dao().save(neoTransaction);
    }

    public static RewardPeriodDAO dao() {
        return NetworkDAOImpl.getDAO(RewardPeriod.class);
    }
}
