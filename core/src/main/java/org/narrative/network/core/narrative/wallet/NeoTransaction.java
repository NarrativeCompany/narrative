package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.narrative.wallet.dao.NeoTransactionDAO;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.neo.services.NeoscanTransactionMetadata;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.AccessLevel;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

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
public class NeoTransaction implements DAOObject<NeoTransactionDAO> {

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private NeoTransactionType type;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_neoTransaction_fromNeoWallet")
    private NeoWallet fromNeoWallet;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_neoTransaction_toNeoWallet")
    private NeoWallet toNeoWallet;

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue nrveAmount;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy=NeoTransactionId.Fields.neoTransaction)
    @OrderBy(NeoTransactionId.Fields.transactionDatetime)
    private List<NeoTransactionId> neoTransactionIds;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient boolean hasInitializedWalletTransaction;

    @Transient
    @Setter(AccessLevel.NONE)
    private transient WalletTransaction walletTransaction;

    public NeoTransaction(WalletTransaction walletTransaction) {
        this(walletTransaction.getType().getNeoTransactionType(), walletTransaction.getType().getNeoTransactionType().getFromNeoWallet(walletTransaction), walletTransaction.getType().getNeoTransactionType().getToNeoWallet(walletTransaction), walletTransaction.getNrveAmount());
    }

    public NeoTransaction(NeoTransactionType type, NeoWallet fromNeoWallet, NeoWallet toNeoWallet, NrveValue nrveAmount) {
        assert type != null : "Should always have a type for the reward!";
        assert exists(fromNeoWallet) && exists(toNeoWallet) : "Should always specify both the from and to wallets for the transaction!";
        assert type.isValidTransaction(fromNeoWallet, toNeoWallet) : "The supplied wallet profile must be valid for this type of transaction";
        assert nrveAmount!=null : "Should always specify a nrveAmount!";

        this.type = type;
        this.fromNeoWallet = fromNeoWallet;
        this.toNeoWallet = toNeoWallet;
        this.nrveAmount = nrveAmount;

        this.neoTransactionIds = new LinkedList<>();
    }

    public WalletTransaction getWalletTransaction() {
        if(!hasInitializedWalletTransaction) {
            walletTransaction = WalletTransaction.dao().getForNeoTransaction(this);
            assert getType().getWalletTransactionType()==null || exists(walletTransaction) : "Failed lookup of a WalletTransaction for a NeoTransaction that should have one! oid/" + getOid();
            hasInitializedWalletTransaction = true;
        }
        return walletTransaction;
    }

    public NeoTransactionId addNeoTransaction(NeoscanTransactionMetadata metadata) {
        NeoTransactionId neoTransactionId = new NeoTransactionId(this, metadata);
        getNeoTransactionIds().add(neoTransactionId);
        NeoTransactionId.dao().save(neoTransactionId);
        return neoTransactionId;
    }

    public static NeoTransactionDAO dao() {
        return NetworkDAOImpl.getDAO(NeoTransaction.class);
    }
}
