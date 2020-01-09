package org.narrative.network.core.master.admin.actions.rewards;

import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import com.opensymphony.xwork2.Preparable;

import java.util.List;

/**
 * Date: 2019-07-01
 * Time: 15:53
 *
 * @author brian
 */
class RewardsRedemptionsBaseAction extends ClusterAction implements Preparable {
    List<WalletTransaction> pendingRedemptions;
    List<WalletTransaction> processingRedemptions;

    @Override
    public void prepare() throws Exception {
        processingRedemptions = WalletTransaction.dao().getForTypeAndStatus(WalletTransactionType.USER_REDEMPTION, WalletTransactionStatus.PROCESSING);
        // bl: only support pending redemptions if none are currently processing
        if(processingRedemptions.isEmpty()) {
            pendingRedemptions = WalletTransaction.dao().getForTypeAndStatus(WalletTransactionType.USER_REDEMPTION, WalletTransactionStatus.PENDING);
        }
    }
}
