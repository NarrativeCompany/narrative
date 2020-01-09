package org.narrative.network.core.narrative.wallet.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-15
 * Time: 09:06
 *
 * @author jonmark
 */
public class ProcessWalletTransactionTask extends AreaTaskImpl<WalletTransaction> {
    private final OID fromWalletOid;
    private final OID toWalletOid;
    private final WalletTransactionType type;
    private final WalletTransactionStatus status;
    private final NrveValue amount;

    private String memo;

    public ProcessWalletTransactionTask(Wallet fromWallet, Wallet toWallet, WalletTransactionType type, NrveValue amount) {
        this(fromWallet, toWallet, type, WalletTransactionStatus.COMPLETED, amount);
    }

    public ProcessWalletTransactionTask(Wallet fromWallet, Wallet toWallet, WalletTransactionType type, WalletTransactionStatus status, NrveValue amount) {
        super(true);

        if (!type.isValidTransaction(fromWallet, toWallet)) {
            throw UnexpectedError.getRuntimeException("Attempting to issue transaction with unsupported wallet types.");
        }

        if (amount == null) {
            throw UnexpectedError.getRuntimeException("Should always provide a non-null transaction amount.");
        }

        if (amount.compareTo(NrveValue.ZERO) < 0) {
            // jw: currently we only support negative transactions from wallets that support negative values to wallets that support negative values.
            if (!exists(fromWallet) || !fromWallet.getType().isSupportsNegativeBalances()) {
                throw UnexpectedError.getRuntimeException("Should always be provided with a fromWallet that supports negative values when processing a negative transaction!");
            }
            if (!exists(toWallet) || !toWallet.getType().isSupportsNegativeBalances()) {
                throw UnexpectedError.getRuntimeException("Should always be provided with a toWallet that supports negative values when processing a negative transaction!");
            }
        }

        this.type = type;
        this.status = status;
        this.amount = amount;

        // jw: we need to select both of these for update (locking them in the same transaction), so let's just store the OID
        fromWalletOid = exists(fromWallet) ? fromWallet.getOid() : null;
        toWalletOid = exists(toWallet) ? toWallet.getOid() : null;;
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        // jw: Let's lock both wallets in a single request
        List<OID> walletOids = new ArrayList<>(2);
        if (fromWalletOid != null) {
            walletOids.add(fromWalletOid);
        }
        if (toWalletOid != null) {
            walletOids.add(toWalletOid);
        }
        assert !walletOids.isEmpty() : "We should have at least one wallet for this transaction!";

        Map<OID, Wallet> walletLookup = Wallet.dao().getIDToObjectsFromObjects(Wallet.dao().getLockedObjectsFromIDs(walletOids));

        // jw: now that we have the wallets, let's get the objects from the lookup.
        Wallet fromWallet = fromWalletOid != null ? walletLookup.get(fromWalletOid) : null;
        Wallet toWallet = toWalletOid != null ? walletLookup.get(toWalletOid) : null;

        // jw: finally, let's confirm that we got wallets for each OID
        assert fromWalletOid != null == exists(fromWallet) : "If we were given a fromWalletOid then we should have ended up with a Wallet from it.";
        assert toWalletOid != null == exists(toWallet) : "If we were given a toWalletOid then we should have ended up with a Wallet from it.";

        // jw: Now that locking is behind us, we can create the transaction
        WalletTransaction transaction = new WalletTransaction(fromWallet, toWallet, type, status, amount);

        // jw: even if the memo is null let's just set it, no point wrapping this in a if.
        transaction.setMemo(!isEmpty(memo) ? memo : null);

        WalletTransaction.dao().save(transaction);

        // jw: now that the transaction is setup in Hibernate to get created, let's adjust the wallets
        if (transaction.getFromWallet()!=null) {
            transaction.getFromWallet().removeFunds(transaction.getNrveAmount());
        }
        if (transaction.getToWallet()!=null) {
            transaction.getToWallet().addFunds(transaction.getNrveAmount());
        }

        return transaction;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
