package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.GBigDecimal;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.narrative.rewards.ContentCreatorRewardRole;
import org.narrative.network.core.narrative.wallet.dao.WalletTransactionDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.util.NetworkLogger;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/4/18
 * Time: 11:25 AM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(name = "uidx_walletTransaction_neoTransaction", columnNames = {WalletTransaction.FIELD__NEO_TRANSACTION__COLUMN})})
public class WalletTransaction implements DAOObject<WalletTransactionDAO> {
    private static final NetworkLogger logger = new NetworkLogger(WalletTransaction.class);

    private static final String FIELD__NEO_TRANSACTION__NAME = "neoTransaction";
    static final String FIELD__NEO_TRANSACTION__COLUMN = FIELD__NEO_TRANSACTION__NAME + "_" + NeoTransaction.FIELD__OID__NAME;

    private OID oid;
    private Wallet fromWallet;
    private Wallet toWallet;
    private WalletTransactionType type;
    private WalletTransactionStatus status;
    private Instant transactionDatetime;
    private NrveValue nrveAmount;
    private String memo;
    // jw: this is nullable and used to track how much the nrve was worth at the time of a redemption request
    private BigDecimal usdAmount;
    private NeoTransaction neoTransaction;

    // transient metadata associated with the transaction for mapping purposes to be displayed in the transaction list
    private transient User userForTransactionList;
    private transient User metadataUser;
    private transient Niche metadataNiche;
    private transient Content metadataPost;
    private transient ContentCreatorRewardRole metadataContentCreatorRewardRole;
    private transient Integer metadataActivityBonusPercentage;
    private transient String metadataNeoWalletAddress;
    private transient String metadataNeoTransactionId;

    public WalletTransaction(Wallet fromWallet, Wallet toWallet, WalletTransactionType type, WalletTransactionStatus status, NrveValue nrveAmount) {
        assert exists(fromWallet) || exists(toWallet) : "Should always specify a wallet for the transaction to either go into or out of!";
        assert type != null : "Should always have a type for the reward!";
        assert type.isValidTransaction(fromWallet, toWallet) : "The supplied wallet profile must be valid for this type of transaction";
        assert nrveAmount != null : "Should always have a nrveAmount for the transaction!";
        assert nrveAmount.compareTo(NrveValue.ZERO) >= 0 || (
                exists(fromWallet) && fromWallet.getType().isSupportsNegativeBalances() &&
                exists(toWallet) && toWallet.getType().isSupportsNegativeBalances()
        ) : "The from and to wallets must both be provided and support negative values for negative transactions!";
        assert type.getSupportedStatuses().contains(status) : "Should never create transaction with status/" + status + " for type/" + type;

        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.type = type;
        this.status = status;
        this.nrveAmount = nrveAmount;
        this.transactionDatetime = Instant.now();

        // bl: if there is a NeoTransactionType associated with this transaction type, then we should also record it!
        if(type.getNeoTransactionType()!=null) {
            // bl: this is just a placeholder transaction. we'll need to actually complete the transfer on the blockchain
            // and then record the transaction ID and metadata on this record later.
            NeoTransaction neoTransaction = new NeoTransaction(this);
            setNeoTransaction(neoTransaction);
        }
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_walletTransaction_fromWallet")
    public Wallet getFromWallet() {
        return fromWallet;
    }

    public void setFromWallet(Wallet fromWallet) {
        this.fromWallet = fromWallet;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_walletTransaction_toWallet")
    public Wallet getToWallet() {
        return toWallet;
    }

    public void setToWallet(Wallet toWallet) {
        this.toWallet = toWallet;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public WalletTransactionType getType() {
        return type;
    }

    public void setType(WalletTransactionType type) {
        this.type = type;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public WalletTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(WalletTransactionStatus status) {
        this.status = status;
    }

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    public Instant getTransactionDatetime() {
        return transactionDatetime;
    }

    public void setTransactionDatetime(Instant transactionDatetime) {
        this.transactionDatetime = transactionDatetime;
    }

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    public NrveValue getNrveAmount() {
        return nrveAmount;
    }

    public void setNrveAmount(NrveValue nrveAmount) {
        this.nrveAmount = nrveAmount;
    }

    public void adjustFiatValue(NrveValue adjustment) {
        assert getStatus().isPendingFiatAdjustment() : "Should only ever call this method on a transaction pending fiat adjustment.";
        assert getFromWallet() == null : "We should never be given a fromWallet on transactions using this method.";
        assert exists(getToWallet()) : "We should always be given a toWallet on transactions using this method.";

        // jw: first, let's apply the adjustment locally.
        setNrveAmount(getNrveAmount().add(adjustment));

        if(logger.isDebugEnabled()) logger.debug("Applying fiat adjustment amount of " + adjustment + " NRVE to transaction/" + getOid() + " with Wallet/" + getToWallet());

        // jw: make sure to adjust the wallet by the same amount.
        getToWallet().addFunds(adjustment);

        // jw: finally, mark the transaction as complete now.
        setStatus(WalletTransactionStatus.COMPLETED);
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(BigDecimal usdAmount) {
        this.usdAmount = usdAmount;
    }

    @Transient
    public GBigDecimal getUsdAmountAsGBigDecimal() {
        if(getUsdAmount()==null) {
            return null;
        }
        return new GBigDecimal(getUsdAmount());
    }

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_walletTransaction_neoTransaction")
    public NeoTransaction getNeoTransaction() {
        return neoTransaction;
    }

    public void setNeoTransaction(NeoTransaction neoTransaction) {
        this.neoTransaction = neoTransaction;
    }

    @Transient
    public void setUserForTransactionList(User userForTransactionList) {
        this.userForTransactionList = userForTransactionList;
    }

    @Transient
    public User getMetadataUser() {
        return metadataUser;
    }

    public void setMetadataUser(User metadataUser) {
        this.metadataUser = metadataUser;
    }

    @Transient
    public Niche getMetadataNiche() {
        return metadataNiche;
    }

    public void setMetadataNiche(Niche metadataNiche) {
        this.metadataNiche = metadataNiche;
    }

    @Transient
    public Content getMetadataPost() {
        return metadataPost;
    }

    public void setMetadataPost(Content metadataPost) {
        this.metadataPost = metadataPost;
    }

    @Transient
    public ContentCreatorRewardRole getMetadataContentCreatorRewardRole() {
        return metadataContentCreatorRewardRole;
    }

    public void setMetadataContentCreatorRewardRole(ContentCreatorRewardRole metadataContentCreatorRewardRole) {
        this.metadataContentCreatorRewardRole = metadataContentCreatorRewardRole;
    }

    @Transient
    public Integer getMetadataActivityBonusPercentage() {
        return metadataActivityBonusPercentage;
    }

    public void setMetadataActivityBonusPercentage(Integer metadataActivityBonusPercentage) {
        this.metadataActivityBonusPercentage = metadataActivityBonusPercentage;
    }

    @Transient
    public String getMetadataNeoWalletAddress() {
        if(metadataNeoWalletAddress==null) {
            // bl: NEO wallet address only applies to redemptions
            if(getType().isUserRedemption()) {
                metadataNeoWalletAddress = getFromWallet().getNeoWallet().getNeoAddress();
            }
        }
        return metadataNeoWalletAddress;
    }

    @Transient
    public String getMetadataNeoTransactionId() {
        if(metadataNeoTransactionId==null) {
            // bl: NEO transaction ID only applies to completed redemptions
            if(getType().isUserRedemption() && getStatus().isCompleted()) {
                // bl: there should only ever be one transaction ID per redemption
                metadataNeoTransactionId = getNeoTransaction().getNeoTransactionIds().get(0).getTransactionId();
            }
        }
        return metadataNeoTransactionId;
    }

    @Transient
    public NrveValue getAmount() {
        assert exists(userForTransactionList) : "Should have already set a userForTransactionList before calling this!";
        // bl: transactions to this wallet are positive, so just return the value!
        if(isEqual(userForTransactionList.getWallet(), getToWallet())) {
            return getNrveAmount();
        }
        // bl: otherwise, we need to negate the return value
        return new NrveValue(-getNrveAmount().toNeurons());
    }

    @Transient
    public Wallet getOtherWallet() {
        assert exists(userForTransactionList) : "Should have already set a userForTransactionList before calling this!";
        assert getType().isUserToUserTransaction() : "Should only attempt to get other wallet for user-to-user transactions!";
        Wallet wallet = userForTransactionList.getWallet();

        if(getFromWallet().equals(wallet)) {
            return getToWallet();
        }

        assert getToWallet().equals(wallet) : "Tips should always be either from or to the specified wallet! transaction/" + getOid() + " wallet/" + wallet.getOid();
        return getFromWallet();
    }

    public static WalletTransactionDAO dao() {
        return NetworkDAOImpl.getDAO(WalletTransaction.class);
    }
}
