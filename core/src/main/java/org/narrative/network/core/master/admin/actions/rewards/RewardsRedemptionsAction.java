package org.narrative.network.core.master.admin.actions.rewards;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 09:00
 *
 * @author brian
 */
public class RewardsRedemptionsAction extends RewardsRedemptionsBaseAction {
    public static final String ACTION_NAME = "rewards-redemptions";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private NrveUsdValue pendingRedemptionsTotal;
    private NrveUsdValue processingRedemptionsTotal;

    public String input() throws Exception {
        if(!isEmptyOrNull(pendingRedemptions)) {
            NrveValue total = pendingRedemptions.stream().map(WalletTransaction::getNrveAmount).reduce(NrveValue.ZERO, NrveValue::add);
            pendingRedemptionsTotal = new NrveUsdValue(total);
        }
        if(!isEmptyOrNull(processingRedemptions)) {
            NrveValue total = processingRedemptions.stream().map(WalletTransaction::getNrveAmount).reduce(NrveValue.ZERO, NrveValue::add);
            processingRedemptionsTotal = new NrveUsdValue(total);
        }
        return INPUT;
    }

    public List<WalletTransaction> getPendingRedemptions() {
        return pendingRedemptions;
    }

    public NrveUsdValue getPendingRedemptionsTotal() {
        return pendingRedemptionsTotal;
    }

    public List<WalletTransaction> getProcessingRedemptions() {
        return processingRedemptions;
    }

    public NrveUsdValue getProcessingRedemptionsTotal() {
        return processingRedemptionsTotal;
    }
}
