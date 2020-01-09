package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.UserNeoWalletDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.RequestRedemptionInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserNeoWalletInput;

/**
 * Date: 2019-06-26
 * Time: 12:31
 *
 * @author jonmark
 */
public interface RedemptionService {
    UserNeoWalletDTO updateWalletNeoAddressForCurrentUser(UpdateUserNeoWalletInput input);

    UserNeoWalletDTO deleteWalletNeoAddressForCurrentUser(UpdateProfileAccountConfirmationInputBase input);

    UserNeoWalletDTO getWalletNeoAddressForCurrentUser();

    void requestNeoWalletRedemptionForCurrentUser(RequestRedemptionInput input);

    void deleteNeoWalletRedemptionForCurrentUser(OID redemptionOid);
}
