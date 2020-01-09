package org.narrative.network.core.master.admin.actions.rewards;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.JSONMap;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.SendUserRedemptionProcessedEmail;
import org.narrative.network.customizations.narrative.neo.services.NeoscanTransactionMetadata;
import org.narrative.network.customizations.narrative.neo.services.NeoscanUtils;
import org.narrative.network.shared.struts.NetworkResponses;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 09:00
 *
 * @author brian
 */
public class MarkProcessingRewardsRedemptionsCompleteAction extends RewardsRedemptionsBaseAction {
    public static final String ACTION_NAME = "mark-processing-rewards-redemptions-complete";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    public static final String JSON_FILE_DATA_PARAM = "jsonFileData";

    private File jsonFileData;

    private final Map<OID, NeoscanTransactionMetadata> transactionOidToMetadata = new HashMap<>();

    @Override
    public void prepare() throws Exception {
        super.prepare();

        if(processingRedemptions.isEmpty()) {
            throw UnexpectedError.getRuntimeException("Can't mark processing rewards redemptions as complete when there are no redemptions processing!");
        }
    }

    @Override
    public void validate() {
        JSONMap jsonMap = null;
        try {
            if (jsonFileData != null && jsonFileData.exists()) {
                String json = FileUtils.readFileToString(jsonFileData);
                jsonMap = JSONMap.getJsonMap(json);
            }
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed reading JSON file!", e);
        }

        if(jsonMap==null) {
            throw new ApplicationError("completed-bulk-transfer-jobs.json file is required!");
        }

        // bl: lots of validation to do to ensure the data is correct
        // first, let's make sure every transaction OID that we expect is in the JSON
        Set<OID> allTransactionOids = processingRedemptions.stream().map(WalletTransaction::getOid).collect(Collectors.toSet());
        List<OID> transactionOidsInJson = OID.getOIDList(jsonMap.getInternalMap().keySet());
        if(!CollectionUtils.isEqualCollection(allTransactionOids, transactionOidsInJson)) {
            throw new ApplicationError("Transaction mismatch between processing transactions and JSON! Difference: " + CollectionUtils.disjunction(allTransactionOids, transactionOidsInJson));
        }

        Collection<String> neoTransactionIds = (Collection)jsonMap.getInternalMap().values();

        // bl: identify the Redemption Temp wallet by looking at a single processing redemption since they should all be the same
        String redemptionTempNeoWalletAddress = processingRedemptions.get(0).getNeoTransaction().getFromNeoWallet().getNeoAddress();
        // next, let's validate all of the transactions on the blockchain via Neoscan
        Map<String, NeoscanTransactionMetadata> transactionIdToMetadata;
        try {
            transactionIdToMetadata = NeoscanUtils.getTransactionMetadatas(redemptionTempNeoWalletAddress, neoTransactionIds);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed looking up transactions on Neoscan!", e);
        }

        // make sure we found all of the transactions
        if(!CollectionUtils.isEqualCollection(transactionIdToMetadata.keySet(), neoTransactionIds)) {
            throw new ApplicationError("Not all transactions could be identified on Neoscan! Missing: " + CollectionUtils.disjunction(transactionIdToMetadata.keySet(), neoTransactionIds));
        }

        for (WalletTransaction transaction : processingRedemptions) {
            String transactionId = jsonMap.getString(transaction.getOid().toString());
            // bl: skip anything we can't find. we'll validate at the end.
            if(isEmpty(transactionId)) {
                continue;
            }
            NeoscanTransactionMetadata metadata = transactionIdToMetadata.get(transactionId);
            if(metadata==null) {
                continue;
            }
            transactionOidToMetadata.put(transaction.getOid(), metadata);
        }

        if(!transactionOidToMetadata.keySet().containsAll(allTransactionOids)) {
            throw new ApplicationError("Not all transaction metadata was found! Missing: " + CollectionUtils.disjunction(transactionOidToMetadata.keySet(), allTransactionOids));
        }
    }

    public String execute() throws Exception {
        Area narrativePlatformArea = Area.dao().getNarrativePlatformArea();
        // bl: there should be no need to lock wallets up front since this action doesn't actually affect
        // properties of the wallet (such as the balance)

        // bl: once all of the data above has been fetched, the final piece here is easy! just mark the transactions
        // as COMPLETED and associate the transaction metadata
        for (WalletTransaction transaction : processingRedemptions) {
            NeoscanTransactionMetadata metadata = transactionOidToMetadata.get(transaction.getOid());
            NeoTransaction neoTransaction = transaction.getNeoTransaction();
            neoTransaction.addNeoTransaction(metadata);
            // bl: the wallet balances should have already been updated, so there should be nothing else to do here
            // bl: note that the transaction date of the WalletTransaction will reflect the date of the original
            // redemption request, which is by design since that's the date used for USD valuation calculation.
            transaction.setStatus(WalletTransactionStatus.COMPLETED);

            User user = transaction.getFromWallet().getUser();

            // bl: finally, send the user an email letting them know the redemption has been processed
            getNetworkContext().doAreaTask(narrativePlatformArea, new SendUserRedemptionProcessedEmail(user, transaction));
        }
        return NetworkResponses.redirectResponse();
    }

    public void setJsonFileData(File jsonFileData) {
        this.jsonFileData = jsonFileData;
    }
}
