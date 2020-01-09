package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.customizations.narrative.service.api.model.NeoWalletDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Date: 10/1/19
 * Time: 1:20 PM
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public abstract class NeoWalletMapper {
    public abstract NeoWalletDTO mapNeoWalletToDto(NeoWallet neoWallet);

    public abstract List<NeoWalletDTO> mapNeoWalletsToDtos(List<NeoWallet> neoWallets);
}
