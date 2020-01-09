package org.narrative.network.core.master.admin.actions.rewards;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.shared.struts.NetworkResponses;

import java.util.List;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-01
 * Time: 15:53
 *
 * @author brian
 */
public class ProcessRewardsRedemptionsAction extends RewardsRedemptionsBaseAction {
    public static final String ACTION_NAME = "process-rewards-redemptions";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    public static final String REDEMPTION_TEMP_WALLET_NEO_ADDRESS_PARAM = "redemptionTempWalletNeoAddress";

    private String redemptionTempWalletNeoAddress;

    private NeoWallet redemptionTempNeoWallet;

    @Override
    public void validate() {
        if(!processingRedemptions.isEmpty()) {
            throw UnexpectedError.getRuntimeException("Can't start processing rewards redemptions when there are redemptions already processing!");
        }
        if(pendingRedemptions.isEmpty()) {
            throw UnexpectedError.getRuntimeException("Can't start processing rewards redemptions when there aren't any to process!");
        }

        if(NeoUtils.validateNeoAddress(this, "redemptionTempWalletNeoAddress", "Redemption Temp Wallet NEO Address", redemptionTempWalletNeoAddress)) {
            // bl: extra sanity check to make sure that this wallet hasn't been used before
            redemptionTempNeoWallet = NeoWallet.dao().getForNeoAddress(redemptionTempWalletNeoAddress);
            // bl: allow redemption temp wallets to be re-used
            if(exists(redemptionTempNeoWallet) && !redemptionTempNeoWallet.getType().isRedemptionTemp()) {
                addFieldError("redemptionTempWalletNeoAddress", "This NEO address is already in use.");
            }
        }
    }

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String execute() throws Exception {
        // bl: create the Redemption temp wallet that we're going to use for transaction processing (if it doesn't already exist)
        if(!exists(redemptionTempNeoWallet)) {
            redemptionTempNeoWallet = new NeoWallet(NeoWalletType.REDEMPTION_TEMP);
            redemptionTempNeoWallet.setNeoAddress(redemptionTempWalletNeoAddress);
            NeoWallet.dao().save(redemptionTempNeoWallet);
        }

        // bl: lock all of the wallets up front to avoid race conditions with cancellations. even though we aren't
        // going to adjust balances below, we still need to lock.
        List<OID> walletOids = pendingRedemptions.stream().map(t -> t.getFromWallet().getOid()).collect(Collectors.toList());
        Wallet.dao().getLockedObjectsFromIDs(walletOids);
        // bl: go through all of the pending redemptions and:
        //   1. mark each transaction as processing
        //   2. create the NeoTransaction

        NrveValue totalNrve = NrveValue.ZERO;
        for (WalletTransaction transaction : pendingRedemptions) {
            assert transaction.getStatus().isPending() : "Somehow found a transaction that is not pending! status/" + transaction.getStatus();

            // set the status to processing. must do this before attempting to update the balance since PENDING
            // transactions don't affect wallet balances :)
            transaction.setStatus(WalletTransactionStatus.PROCESSING);

            NeoWallet userNeoWallet = transaction.getFromWallet().getNeoWallet();

            // create a NeoTransaction
            NeoTransaction neoTransaction = new NeoTransaction(NeoTransactionType.MEMBER_CREDITS_REDEMPTION, redemptionTempNeoWallet, userNeoWallet, transaction.getNrveAmount());
            NeoTransaction.dao().save(neoTransaction);

            // associate the NeoTransaction with the user's WalletTransaction
            transaction.setNeoTransaction(neoTransaction);

            totalNrve = totalNrve.add(transaction.getNrveAmount());
        }

        // bl: finally, create a NeoTransaction to transfer the total NRVE amount from the Member Credits wallet
        // to the Redemption Temp wallet
        NeoWallet memberCreditsWallet = NeoWallet.dao().getSingletonWallet(NeoWalletType.MEMBER_CREDITS);
        NeoTransaction neoTransaction = new NeoTransaction(NeoTransactionType.MEMBER_CREDITS_BULK_REDEMPTION, memberCreditsWallet, redemptionTempNeoWallet, totalNrve);
        NeoTransaction.dao().save(neoTransaction);

        return NetworkResponses.redirectResponse();
    }

    public void setRedemptionTempWalletNeoAddress(String redemptionTempWalletNeoAddress) {
        this.redemptionTempWalletNeoAddress = redemptionTempWalletNeoAddress;
    }
}
