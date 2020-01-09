package org.narrative.network.customizations.narrative.service.impl.redemption;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserNeoWalletInput;
import org.narrative.network.customizations.narrative.service.impl.user.UpdateProfileAccountConfirmationBaseTask;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-26
 * Time: 13:16
 *
 * @author jonmark
 */
public class UpdateUserNeoWalletTask extends UpdateProfileAccountConfirmationBaseTask<Object> {
    private final String neoAddress;

    private NeoWallet neoWallet;

    public UpdateUserNeoWalletTask(User user, UpdateUserNeoWalletInput input) {
        super(user, input);
        neoAddress = input.getNeoAddress();
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        if (!getUser().getRedemptionStatus().isWalletUpdatable()) {
            throw UnexpectedError.getRuntimeException("Should never use this task for users who cannot update their neo address!");
        }

        // bl: always validate the NEO address. it's invalid to provide an empty address here now.
        if (NeoUtils.validateNeoAddress(validationContext, UpdateUserNeoWalletInput.Fields.neoAddress, neoAddress)) {
            // jw: let's try and get the existing wallets for this address.
            neoWallet = NeoWallet.dao().getForNeoAddress(neoAddress);

            if (exists(neoWallet)) {
                if(!neoWallet.getType().isUser()) {
                    validationContext.addFieldError(UpdateUserNeoWalletInput.Fields.neoAddress, "updateUserWalletNeoAddressTask.neoAddressReserved");
                } else {
                    // bl: if the address is the user's current address, then that's also a validation error
                    if(isEqual(getUser().getWallet().getNeoWallet(), neoWallet)) {
                        validationContext.addFieldError(UpdateUserNeoWalletInput.Fields.neoAddress, "updateUserWalletNeoAddressTask.alreadyCurrentNeoAddress");
                    }
                }
            }
        }
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: if we did not find a NeoWallet then create one.
        if (!exists(neoWallet)) {
            neoWallet = new NeoWallet(NeoWalletType.USER);
            neoWallet.setNeoAddress(neoAddress);

            NeoWallet.dao().save(neoWallet);
        }

        // jw: now that the above is taken care of we have a simple job ahead of us. Use the utility method on user to
        //     update the neoAddress. This will ensure that all ancillary data gets updated along with the wallet.
        getUser().updateNeoWallet(neoWallet);

        return null;
    }
}
