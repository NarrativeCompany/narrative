package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.JUnitUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.wallet.dao.WalletDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.NrveValueDetail;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.LockMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * Date: 2019-05-14
 * Time: 15:31
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
        @UniqueConstraint(name = "uidx_wallet_type", columnNames = {Wallet.FIELD__TYPE__COLUMN, Wallet.FIELD__SINGLETON__COLUMN}),
        @UniqueConstraint(name = "uidx_wallet_neoWallet", columnNames = {Wallet.FIELD__NEO_WALLET__COLUMN, Wallet.FIELD__ALLOWS_SHARED_NEO_WALLETS__COLUMN})
})
public class Wallet implements DAOObject<WalletDAO> {
    private static final String FIELD__TYPE__NAME = "type";
    static final String FIELD__TYPE__COLUMN = FIELD__TYPE__NAME;
    private static final String FIELD__SINGLETON__NAME = "singleton";
    static final String FIELD__SINGLETON__COLUMN = FIELD__SINGLETON__NAME;
    private static final String FIELD__ALLOWS_SHARED_NEO_WALLETS__NAME = "allowsSharedNeoWallets";
    static final String FIELD__ALLOWS_SHARED_NEO_WALLETS__COLUMN = FIELD__ALLOWS_SHARED_NEO_WALLETS__NAME;
    private static final String FIELD__NEO_WALLET__NAME = "neoWallet";
    static final String FIELD__NEO_WALLET__COLUMN = FIELD__NEO_WALLET__NAME + "_" + NeoWallet.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private WalletType type;

    private Boolean singleton;
    private Boolean allowsSharedNeoWallets;

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue balance;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_wallet_neoWallet")
    private NeoWallet neoWallet;

    @OneToMany(fetch=FetchType.LAZY, mappedBy=WalletTransaction.Fields.toWallet)
    private List<WalletTransaction> toWalletTransactions;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient ProratedMonthRevenue proratedMonthRevenue;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient User user;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient NrveValueDetail balanceDetail;

    public Wallet(WalletType type) {
        this.type = type;
        // bl: the singleton flag is used to enforce uniqueness by wallet type. for types that are not singletons,
        // we use a null value so that uniqueness is not enforced
        this.singleton = type.isSingleton() ? Boolean.TRUE : null;
        // jw: the allowsSharedNeoWallets flag is used to enforce uniqueness on wallets that should never share the same
        //     NeoWallet. The available values are null or false.
        this.allowsSharedNeoWallets = type.isAllowsSharedNeoWallets() ? null : Boolean.FALSE;
        // jw: we need to be sure to initialize the balance at 0.
        this.balance = NrveValue.ZERO;

        // bl: also create a NeoWallet if there should be one!
        // bl: if the WalletType is not a singleton, but the NeoWalletType is a singleton (e.g. for USER and REWARD_PERIOD wallets),
        // then we do _not_ want to create the NeoWallet here!
        // simplified: if the singleton status of the wallet types matches, then they should always be paired up!
        if(type.isSameCardinalityAsNeoWalletType()) {
            NeoWallet neoWallet = new NeoWallet(type.getNeoWalletType());
            // bl: by default, this NEO wallet won't have an address. it'll have to be added later
            setNeoWallet(neoWallet);
        }
    }

    public void addFunds(NrveValue funds) {
        assertWalletLocked();

        setBalance(getBalance().add(funds));

        validateBalance();
    }

    public void removeFunds(NrveValue funds) {
        assertWalletLocked();

        setBalance(getBalance().subtract(funds));

        validateBalance();
    }

    private void assertWalletLocked() {
        // bl: in order for tests to work, handle when we're running tests (in which case either the DAO might not
        // exist OR the Wallet might not be locked).
        // bl: note that the order here is intentional so that we only check the stack for JUnit if the test
        // that we expect to pass fails. this avoids the overhead of examining the stack in the non-testing scenarios.
        assert (Wallet.dao()!=null && Wallet.dao().isLocked(this, LockMode.PESSIMISTIC_WRITE)) || JUnitUtil.isJUnitTest() : "Should always have locked the wallet before adjusting the funds balance!";
    }

    private void validateBalance() {
        if (getBalance().compareTo(NrveValue.ZERO) < 0 && !getType().isSupportsNegativeBalances()) {
            throw UnexpectedError.getRuntimeException("Funds change caused wallet to go negative on type that doesn't support negative balances. Transaction should have been prevented before it got here! oid/" + getOid());
        }
    }

    public ProratedMonthRevenue getProratedMonthRevenue() {
        assert getType().isProratedMonthRevenue() : "Should never attempt to get ProratedMonthRevenue for a wallet of a different type! type/" + getType();
        if(proratedMonthRevenue==null) {
            proratedMonthRevenue = ProratedMonthRevenue.dao().getForWallet(this, LockModeType.NONE);
        }
        return proratedMonthRevenue;
    }

    public User getUser() {
        assert getType().isUser() : "Should never attempt to get User for a wallet of a different type! type/" + getType();
        if(user==null) {
            user = User.dao().getFirstBy(new NameValuePair<>(User.FIELD__WALLET__NAME, this));
        }
        return user;
    }

    public NrveValueDetail getBalanceDetail() {
        if (balanceDetail==null) {
            balanceDetail = new NrveValueDetail(getBalance());
        }
        return balanceDetail;
    }

    public static WalletDAO dao() {
        return NetworkDAOImpl.getDAO(Wallet.class);
    }
}
