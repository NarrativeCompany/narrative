package org.narrative.network.core.user.services;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.YearMonth;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-21
 * Time: 11:32
 *
 * @author jonmark
 */
public class ReclaimNrveFromDeletedUserTask extends AreaTaskImpl<Object> {
    private final User deletedUser;

    public ReclaimNrveFromDeletedUserTask(User deletedUser) {
        super(true);
        assert deletedUser.isDeleted() : "Shouild only ever call this task on users who are deleted.";

        this.deletedUser = deletedUser;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: if the user does not have any balance in their personal wallet then there is nothing for us to do.
        // jw: note: I am also leaving behind any negative balances. That should never happen, but still, we should not
        //     deduct it from the RewardPeriod wallet for it.
        if (deletedUser.getWallet().getBalance().compareTo(NrveValue.ZERO) <= 0) {
            return null;
        }

        // jw: for patching purposes let's go ahead and use the deleted users lastDeactivationDatetime.
        YearMonth yearMonth = RewardUtils.calculateYearMonth(deletedUser.getPreferences().getLastDeactivationDatetime());

        RewardPeriod period = RewardPeriod.dao().getForYearMonth(yearMonth);

        assert exists(period) : "We should always find a period for a deleted user.";
        assert !period.isCompleted() : "The period for the deleted user should not be closed!";

        // jw: now the easy part, transfer everything from the users wallet into period for their deletion.
        getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                deletedUser.getWallet(),
                period.getWallet(),
                WalletTransactionType.DELETED_USER_ABANDONED_BALANCES,
                deletedUser.getWallet().getBalance()
        ));

        assert deletedUser.getWallet().getBalance().compareTo(NrveValue.ZERO) == 0 : "The users balance should always be Zero after transferring their balance.";

        return null;
    }
}
