package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.customizations.narrative.service.api.model.NeoWalletDTO;

import java.util.List;

/**
 * Date: 10/1/19
 * Time: 1:11 PM
 *
 * @author brian
 */
public interface NeoWalletService {
    List<NeoWalletDTO> getNeoWallets();
}
