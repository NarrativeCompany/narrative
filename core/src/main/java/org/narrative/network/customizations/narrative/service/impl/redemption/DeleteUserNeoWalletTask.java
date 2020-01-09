package org.narrative.network.customizations.narrative.service.impl.redemption;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.impl.user.UpdateProfileAccountConfirmationBaseTask;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-18
 * Time: 9:57
 *
 * @author brian
 */
public class DeleteUserNeoWalletTask extends UpdateProfileAccountConfirmationBaseTask<Object> {

    public DeleteUserNeoWalletTask(User user, UpdateProfileAccountConfirmationInputBase input) {
        super(user, input);
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        if(!getUser().getRedemptionStatus().isWalletUpdatable()) {
            throw UnexpectedError.getRuntimeException("Should never use this task for users who cannot update their neo address!");
        }

        if(!exists(getUser().getWallet().getNeoWallet())) {
            validationContext.addMethodError("deleteUserWalletNeoAddressTask.neoAddressNotSet");
        }
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: just remove the wallet from the user's profile!
        getUser().updateNeoWallet(null);

        return null;
    }
}
