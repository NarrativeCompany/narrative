package org.narrative.network.core.master.admin.actions;

import com.opensymphony.xwork2.Preparable;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.struts.AfterPrepare;
import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.shared.struts.NetworkResponses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 09:00
 *
 * @author brian
 */
public class NeoWalletsAction extends ClusterAction implements Preparable {
    public static final String ACTION_NAME = "neo-wallets";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private List<NeoWallet> neoWallets;

    private final Map<NeoWallet, String> walletToNeoAddress = new HashMap<>();

    @Override
    public void prepare() throws Exception {
        // bl: exclude Redemption Temp wallets from being managed here
        neoWallets = NeoWallet.dao().getAllByTypes(NeoWalletType.MANAGEABLE_TYPES);
        neoWallets.sort(NeoWallet.COMPARATOR);
        walletToNeoAddress.putAll(neoWallets.stream().filter(w -> !isEmpty(w.getNeoAddress())).collect(Collectors.toMap(Function.identity(), NeoWallet::getNeoAddress)));
    }

    @Override
    public void validate() {
        Set<String> neoAddresses = new HashSet<>();
        Iterator<Map.Entry<NeoWallet, String>> iter = walletToNeoAddress.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<NeoWallet, String> entry = iter.next();
            NeoWallet wallet = entry.getKey();
            String neoAddress = entry.getValue();
            if(!exists(wallet)) {
                throw UnexpectedError.getRuntimeException("Missing wallet!");
            }
            // bl: don't allow REDEMPTION_TEMP and USER wallets to be managed directly
            if(!wallet.getType().isAllowsWalletManagement()) {
                iter.remove();
                continue;
            }
            // bl: if the wallet already has an address, then don't allow it to be changed!
            if(!isEmpty(wallet.getNeoAddress())) {
                iter.remove();
                continue;
            }
            // bl: allow empty values to remain empty
            if(isEmpty(neoAddress)) {
                continue;
            }
            if(NeoUtils.validateNeoAddress(this, "walletToNeoAddress['" + wallet.getOid() + "']", wallet.getNameForDisplay(), neoAddress)) {
                if(neoAddresses.contains(neoAddress)) {
                    addFieldError("walletToNeoAddress['" + wallet.getOid() + "']", "Wallet addresses must be unique!");
                } else {
                    neoAddresses.add(neoAddress);
                }
            }
        }
    }

    public String input() throws Exception {
        return INPUT;
    }

    @Override
    public String execute() throws Exception {
        for (Map.Entry<NeoWallet, String> entry : walletToNeoAddress.entrySet()) {
            NeoWallet wallet = entry.getKey();
            String neoAddress = entry.getValue();
            if(isEmpty(neoAddress)) {
                continue;
            }
            assert isEmpty(wallet.getNeoAddress()) : "Should only ever update NeoWallet addresses when empty!";
            wallet.setNeoAddress(neoAddress);
        }
        return NetworkResponses.redirectResponse();
    }

    public List<NeoWallet> getNeoWallets() {
        return neoWallets;
    }

    @AfterPrepare
    public Map<NeoWallet, String> getWalletToNeoAddress() {
        return walletToNeoAddress;
    }
}
