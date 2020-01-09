package org.narrative.network.core.user.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.user.TwoFactorAuthenticationBackupCode;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import static org.narrative.common.util.CoreUtils.*;

/**
 * This task checks against the provided code against the users available backup codes and returns whether it was found.
 * If found, the code will be added to the users used backup codes so that it cannot be used again.
 *
 * Date: 2019-06-18
 * Time: 11:40
 *
 * @author jonmark
 */
public class CheckTwoFactorAuthenticationBackupCodeTask extends AreaTaskImpl<Boolean> {
    private final User user;
    private final int code;

    public CheckTwoFactorAuthenticationBackupCodeTask(User user, int code) {
        assert exists(user) : "A user must be provided to this task!";
        assert user.isTwoFactorAuthenticationEnabled() : "The provided user should always have two factor authentication enabled.";

        this.user = user;
        this.code = code;
    }

    @Override
    protected Boolean doMonitoredTask() {
        for (TwoFactorAuthenticationBackupCode backupCode : user.getAvailableTwoFactorAuthenticationBackupCodes()) {
            if (isEqual(code, backupCode.getBackupCode(user))) {
                OID userOid = user.getOid();
                // jw: Let's allow the current process to continue on, and if it succeeds then let's ensure that the
                //     user cannot use this code again.
                PartitionGroup.addEndOfPartitionGroupRunnable(() -> {
                    TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                        @Override
                        protected Object doMonitoredTask() {
                            User user = User.dao().get(userOid);
                            // jw: there is a chance that this code was used during the disabling of 2FA on this account,
                            //     so let's only mark this code as used if the user still has 2FA enabled.
                            if (user.isTwoFactorAuthenticationEnabled()) {
                                user.getUsedTwoFactorAuthenticationBackupCodes().add(backupCode);
                            }

                            return null;
                        }
                    });
                });

                return true;
            }
        }

        return false;
    }
}
