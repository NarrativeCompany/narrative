package org.narrative.network.core.master.admin.actions.rewards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 2019-06-11
 * Time: 09:00
 *
 * @author brian
 */
public class ProcessingRewardsRedemptionsJsonAction extends RewardsRedemptionsBaseAction {
    public static final String ACTION_NAME = "processing-rewards-redemptions-json";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private String contentDisposition;
    private String stringInput;

    @Override
    public void prepare() throws Exception {
        super.prepare();

        if(processingRedemptions.isEmpty()) {
            throw UnexpectedError.getRuntimeException("Can't export processing rewards redemptions JSON when there are no redemptions processing!");
        }
    }

    public String input() throws Exception {
        NrveValue totalNrve = NrveValue.ZERO;
        Map<String,Object> transfers = new LinkedHashMap<>();
        for (WalletTransaction transaction : processingRedemptions) {
            assert transaction.getStatus().isProcessing() : "Somehow found a transaction that is not processing! status/" + transaction.getStatus();

            NeoWallet userNeoWallet = transaction.getFromWallet().getNeoWallet();

            totalNrve = totalNrve.add(transaction.getNrveAmount());
            Map<String,Object> transferProps = new LinkedHashMap<>();
            transferProps.put("to_address", userNeoWallet.getNeoAddress());
            transferProps.put("amount", transaction.getNrveAmount().toNeurons());
            transfers.put(transaction.getOid().toString(), transferProps);
        }

        Map<String,Object> map = new LinkedHashMap<>();
        map.put("total_transactions", processingRedemptions.size());
        map.put("total_nrve", totalNrve.toNeurons());
        map.put("transfers", transfers);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        stringInput = mapper.writeValueAsString(map);
        contentDisposition = IPHttpUtil.getFileDownloadContentDisposition("transfer-config.json");
        return INPUT;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public String getStringInput() {
        return stringInput;
    }
}
