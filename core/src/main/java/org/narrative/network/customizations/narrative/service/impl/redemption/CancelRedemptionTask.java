package org.narrative.network.customizations.narrative.service.impl.redemption;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.controller.UserController;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-08-06
 * Time: 15:59
 *
 * @author brian
 */
public class CancelRedemptionTask extends AreaTaskImpl<Object> {
    private final Wallet wallet;
    private final OID redemptionOid;

    public CancelRedemptionTask(Wallet wallet, OID redemptionOid) {
        this.wallet = wallet;
        this.redemptionOid = redemptionOid;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: lock up front so that we avoid concurrency issues
        Wallet.dao().refreshForLock(wallet);

        WalletTransaction transaction = WalletTransaction.dao().getForApiParam(redemptionOid, UserController.REDEMPTION_OID_PARAM);

        // bl: only allow this for user redemptions from the current user's wallet! should never happen
        // for any other combination, so this is an unexpected error
        if(!transaction.getType().isUserRedemption()) {
            throw UnexpectedError.getRuntimeException("Wrong transaction type specified!");
        }
        if(getNetworkContext().isHasPrimaryRole() && (!getNetworkContext().getPrimaryRole().isRegisteredUser() || !isEqual(transaction.getFromWallet(), getNetworkContext().getUser().getWallet()))) {
            throw UnexpectedError.getRuntimeException("Invalid transaction for current user!");
        }

        // bl: if the request started processing, then let's give the user a friendly error indicating
        // that the request can't be canceled
        if(!transaction.getStatus().isPending()) {
            if(transaction.getStatus().isProcessing()) {
                throw new ApplicationError(wordlet("redemptionServiceImpl.redemptionStartedProcessing"));
            }
            assert transaction.getStatus().isCompleted() : "Found a redemption transaction with an unexpected status/" + transaction.getStatus() + " oid/" + transaction.getOid();
            throw new ApplicationError(wordlet("redemptionServiceImpl.redemptionCompleted"));
        }

        // bl: update the wallet balance and delete the transaction.
        // bl: doing the opposite of ProcessWalletTransactionTask.
        if (transaction.getFromWallet()!=null) {
            transaction.getFromWallet().addFunds(transaction.getNrveAmount());
        }
        if (transaction.getToWallet()!=null) {
            transaction.getToWallet().removeFunds(transaction.getNrveAmount());
        }

        WalletTransaction.dao().delete(transaction);
        return null;
    }
}
