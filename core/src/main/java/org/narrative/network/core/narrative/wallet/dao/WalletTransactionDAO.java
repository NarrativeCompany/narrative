package org.narrative.network.core.narrative.wallet.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/4/18
 * Time: 11:26 AM
 */
public class WalletTransactionDAO extends GlobalDAOImpl<WalletTransaction, OID> {
    public WalletTransactionDAO() {
        super(WalletTransaction.class);
    }

    public NrveValue getTransactionSum(Collection<WalletTransactionType> types) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSum", NrveValue.class)
                .setParameterList("types", types)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public NrveValue getTransactionSumBetweenWallets(Wallet fromWallet, Wallet toWallet, Set<WalletTransactionType> types) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSumBetweenWallets", NrveValue.class)
                .setParameter("fromWallet", fromWallet)
                .setParameter("toWallet", toWallet)
                .setParameterList("types", types)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public NrveValue getTransactionSumToWallet(Wallet toWallet, Collection<WalletTransactionType> types) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSumToWallet", NrveValue.class)
                .setParameter("toWallet", toWallet)
                .setParameterList("types", types)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public NrveValue getTransactionSumFromWallet(Wallet fromWallet, Collection<WalletTransactionType> types) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSumFromWallet", NrveValue.class)
                .setParameter("fromWallet", fromWallet)
                .setParameterList("types", types)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public Map<WalletTransactionType,NrveValue> getTransactionSumsByTypeToWallet(Wallet toWallet, Collection<WalletTransactionType> types) {
        List<Object[]> objs = getGSession().getNamedQuery("walletTransaction.getTransactionSumsByTypeToWallet")
                .setParameter("toWallet", toWallet)
                .setParameterList("types", types)
                .list();

        Map<WalletTransactionType,NrveValue> typesToSums = new HashMap<>();
        for (Object[] obj : objs) {
            typesToSums.put((WalletTransactionType)obj[0], new NrveValue(((Number)obj[1]).longValue()));
        }
        // bl: make sure the map has an entry for every type!
        for (WalletTransactionType type : types) {
            typesToSums.putIfAbsent(type, NrveValue.ZERO);
        }
        return typesToSums;
    }

    public Map<Wallet, NrveValue> getRefundTransactionSumsByFromWalletInRange(ProratedMonthRevenue proratedMonthRevenue) {
        List<Object[]> objs = getGSession().getNamedQuery("walletTransaction.getRefundTransactionSumsByFromWalletInRange")
                .setParameter("type", proratedMonthRevenue.getType().getRefundTransactionType().getId())
                .setParameter("after", proratedMonthRevenue.getRewardYearMonth().getLowerBoundForQuery())
                .setParameter("before", proratedMonthRevenue.getRewardYearMonth().getUpperBoundForQuery())
                .list();

        Map<OID,Wallet> oidToWallet = Wallet.dao().getIDToObjectsFromIDs(objs.stream().map(obj -> OID.valueOf((Number)obj[0])).collect(Collectors.toSet()));
        Map<Wallet,NrveValue> typesToSums = new HashMap<>();
        for (Object[] obj : objs) {
            OID walletOid = OID.valueOf((Number)obj[0]);
            Wallet wallet = oidToWallet.get(walletOid);
            typesToSums.put(wallet, new NrveValue(((Number)obj[1]).longValue()));
        }
        return typesToSums;
    }

    public long getTransactionCountFromWallet(Wallet fromWallet, Collection<WalletTransactionType> types) {
        return getGSession().createNamedQuery("walletTransaction.getTransactionCountFromWallet", Number.class)
                .setParameter("fromWallet", fromWallet)
                .setParameterList("types", types)
                .uniqueResult()
                .longValue();
    }

    public List<WalletTransaction> getForToWalletAndStatus(Wallet toWallet, WalletTransactionStatus status) {
        return getAllBy(
                new NameValuePair<>(WalletTransaction.Fields.toWallet, toWallet)
                , new NameValuePair<>(WalletTransaction.Fields.status, status)
        );
    }

    public List<WalletTransaction> getForTypeAndStatus(WalletTransactionType type, WalletTransactionStatus status) {
        return getAllBy(
                new NameValuePair<>(WalletTransaction.Fields.type, type)
                , new NameValuePair<>(WalletTransaction.Fields.status, status)
        );
    }

    public List<WalletTransaction> getForFromWalletAndStatusAndType(Wallet fromWallet, WalletTransactionStatus status, WalletTransactionType type) {
        return getAllBy(
                new NameValuePair<>(WalletTransaction.Fields.fromWallet, fromWallet)
                , new NameValuePair<>(WalletTransaction.Fields.status, status)
                , new NameValuePair<>(WalletTransaction.Fields.type, type)
        );
    }

    public NrveValue getSumForToWalletsAndStatus(Collection<Wallet> toWallets, WalletTransactionStatus status) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getSumForToWalletsAndStatus", NrveValue.class)
                .setParameterList("toWallets", toWallets)
                .setParameter("status", status)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public List<WalletTransaction> getForWalletsAndType(Wallet fromWallet, Wallet toWallet, WalletTransactionType type) {
        assert type.getFromWalletType()!=null && type.getToWalletType()!=null : "Should only use this method for transaction types that support both a to and from wallet! not/" + type;
        return getAllBy(
                new NameValuePair<>(WalletTransaction.Fields.fromWallet, fromWallet)
                ,new NameValuePair<>(WalletTransaction.Fields.toWallet, toWallet)
                ,new NameValuePair<>(WalletTransaction.Fields.type, type)
        );
    }

    public WalletTransaction getForFromWalletType(Wallet fromWallet, WalletTransactionType type) {
        return getUniqueBy(
                new NameValuePair<>(WalletTransaction.Fields.fromWallet, fromWallet)
                ,new NameValuePair<>(WalletTransaction.Fields.type, type)
        );
    }

    public List<WalletTransaction> getForToWalletTypeAndAmount(Wallet toWallet, WalletTransactionType type, NrveValue nrveAmount) {
        return getAllBy(
                new NameValuePair<>(WalletTransaction.Fields.toWallet, toWallet)
                ,new NameValuePair<>(WalletTransaction.Fields.type, type)
                ,new NameValuePair<>(WalletTransaction.Fields.nrveAmount, nrveAmount)
        );
    }

    public Map<OID, NrveValue> getUserTransactionSumsFromWallet(Wallet fromWallet) {
        List<Object[]> objs = getGSession().getNamedQuery("walletTransaction.getUserTransactionSumsFromWallet")
                .setParameter("fromWallet", fromWallet)
                .list();
        Map<OID,NrveValue> ret = new LinkedHashMap<>();
        for (Object[] obj : objs) {
            ret.put((OID)obj[0], new NrveValue(((Number)obj[1]).longValue()));
        }
        return ret;
    }

    public List<WalletTransaction> getTransactionsForUser(User user, Collection<WalletTransactionType> excludeTypes, int page, int itemsPerPage) {
        return getGSession().createNamedQuery("walletTransaction.getTransactionsForUser", WalletTransaction.class)
                .setParameter("userWallet", user.getWallet())
                .setParameter("pendingStatus", WalletTransactionStatus.PENDING)
                .setParameter("processingStatus", WalletTransactionStatus.PROCESSING)
                .setParameterList("excludeTypes", excludeTypes)
                .setFirstResult((page-1)*itemsPerPage)
                .setMaxResults(itemsPerPage)
                .list();
    }

    public long getTransactionCountForUser(User user, Collection<WalletTransactionType> excludeTypes) {
        Number count = getGSession().createNamedQuery("walletTransaction.getTransactionCountForUser", Number.class)
                .setParameter("userWallet", user.getWallet())
                .setParameterList("excludeTypes", excludeTypes)
                .uniqueResult();
        return count!=null ? count.longValue() : 0;
    }

    public NrveValue getTransactionSumForWallet(Wallet wallet) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSumForWallet", NrveValue.class)
                .setParameter("wallet", wallet)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }

    public List<WalletTransaction> getTransactionsToWallet(Wallet wallet) {
        return getAllBy(new NameValuePair<>(WalletTransaction.Fields.toWallet, wallet));
    }

    public List<OID> getTransactionOidsFromWallet(Wallet wallet) {
        return getGSession().createNamedQuery("walletTransaction.getTransactionOidsFromWallet", OID.class)
                .setParameter("wallet", wallet)
                .list();
    }

    public Map<WalletTransactionStatus, NrveValue> getTransactionSumByToWalletType(Wallet toWallet, WalletTransactionType type) {
        List<Object[]> objs = getGSession().getNamedQuery("walletTransaction.getTransactionSumByToWalletType")
                .setParameter("toWallet", toWallet)
                .setParameter("type", type)
                .list();
        Map<WalletTransactionStatus, NrveValue> ret = new HashMap<>();
        for (Object[] obj : objs) {
            ret.put((WalletTransactionStatus)obj[0], new NrveValue((Long)obj[1]));
        }
        return ret;
    }

    public WalletTransaction getForNeoTransaction(NeoTransaction neoTransaction) {
        return getUniqueBy(new NameValuePair<>(WalletTransaction.Fields.neoTransaction, neoTransaction));
    }

    public BigDecimal getTotalUsdForNonPendingFromWalletAndTypeAfter(Wallet fromWallet, Collection<WalletTransactionType> types, Instant after) {
        BigDecimal usd = getGSession().createNamedQuery("walletTransaction.getTotalUsdForNonPendingFromWalletAndTypeAfter", BigDecimal.class)
                .setParameterList("types", types)
                .setParameter("fromWallet", fromWallet)
                .setParameter("after", after)
                .setParameter("pendingStatusType", WalletTransactionStatus.PENDING)
                .uniqueResult();
        return usd!=null ? usd : BigDecimal.ZERO;
    }

    public NrveValue getTransactionSumByTypeWithStatus(WalletTransactionType type, WalletTransactionStatus status) {
        NrveValue value = getGSession().createNamedQuery("walletTransaction.getTransactionSumByTypeWithStatus", NrveValue.class)
                .setParameter("type", type)
                .setParameter("status", status)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }
}
