package org.narrative.network.customizations.narrative.service.impl.neowallet;

import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.customizations.narrative.service.api.NeoWalletService;
import org.narrative.network.customizations.narrative.service.api.model.NeoWalletDTO;
import org.narrative.network.customizations.narrative.service.mapper.NeoWalletMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Date: 10/1/19
 * Time: 1:12 PM
 *
 * @author brian
 */
@Service
public class NeoWalletServiceImpl implements NeoWalletService {
    private final NeoWalletMapper neoWalletMapper;

    public NeoWalletServiceImpl(NeoWalletMapper neoWalletMapper) {
        this.neoWalletMapper = neoWalletMapper;
    }

    @Override
    public List<NeoWalletDTO> getNeoWallets() {
        List<NeoWallet> neoWallets = NeoWallet.dao().getAllByTypes(NeoWalletType.MANAGEABLE_TYPES);
        // bl: filter down to wallets that are active
        neoWallets = neoWallets.stream().filter(NeoWallet::isActive).collect(Collectors.toList());
        neoWallets.sort(NeoWallet.COMPARATOR);
        return neoWalletMapper.mapNeoWalletsToDtos(neoWallets);
    }
}
