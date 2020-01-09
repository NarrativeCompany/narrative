package org.narrative.network.core.master.admin.actions;

import com.opensymphony.xwork2.Preparable;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.struts.AfterPrepare;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.customizations.narrative.neo.services.NeoscanTransactionMetadata;
import org.narrative.network.customizations.narrative.neo.services.NeoscanUtils;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-12
 * Time: 07:43
 *
 * @author brian
 */
public class NeoTransactionsAction extends ClusterAction implements Preparable {
    private static final NetworkLogger logger = new NetworkLogger(NeoTransactionsAction.class);

    public static final String ACTION_NAME = "neo-transactions";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;
    public static final String FORM_TYPE = "updateNeoTransactionIdsForm";

    private List<NeoTransaction> neoTransactions;

    private final Map<NeoTransaction, List<String>> transactionToTransactionId = new HashMap<>();
    private final Map<NeoTransaction, List<NeoscanTransactionMetadata>> transactionToMetadata = new HashMap<>();

    @Override
    public void prepare() throws Exception {
        neoTransactions = NeoTransaction.dao().getAllIncomplete(EnumSet.allOf(NeoTransactionType.class).stream().filter(NeoTransactionType::isAllowsTransactionManagement).collect(Collectors.toSet()));
        neoTransactions.sort(INCOMPLETE_NEO_TRANSACTION_COMPARATOR);
    }

    @Override
    public void validate() {
        String nrveScriptHash = getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new AreaTaskImpl<String>(false) {
            @Override
            protected String doMonitoredTask() {
                return getAreaContext().getAuthZone().getSandboxedCommunitySettings().getNrveScriptHash();
            }
        });
        Set<String> uniqueTransactionIds = new HashSet<>();
        Iterator<Map.Entry<NeoTransaction, List<String>>> iter = transactionToTransactionId.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<NeoTransaction, List<String>> entry = iter.next();
            NeoTransaction neoTransaction = entry.getKey();
            if(!exists(neoTransaction)) {
                throw UnexpectedError.getRuntimeException("Missing transaction!");
            }
            // bl: don't allow MEMBER_CREDITS_REDEMPTION transactions to be managed here. there's a separate
            // process for processing redemptions.
            if(!neoTransaction.getType().isAllowsTransactionManagement()) {
                iter.remove();
                continue;
            }

            // bl: make sure that the NeoTransaction's wallets have addresses set. if not, the transfer can't be completed yet
            if(!validateToFromAddressesExist(neoTransaction)) {
                continue;
            }

            // bl: if the NeoTransaction already has an associated transaction, don't allow it to be modified!
            if(!neoTransaction.getNeoTransactionIds().isEmpty()) {
                continue;
            }

            List<String> transactionIdsList = entry.getValue();

            // skip if there aren't any transactions
            if(transactionIdsList==null) {
                continue;
            }

            // bl: remove any empty values
            transactionIdsList.removeIf(IPStringUtil::isEmpty);
            // bl: let's always lowercase the transaction ID since they are case-insensitive hashes
            transactionIdsList.replaceAll(String::toLowerCase);

            Set<String> transactionIds = new HashSet<>(transactionIdsList);

            // bl: allow empty transactions to remain empty
            if(transactionIds.isEmpty()) {
                continue;
            }

            if(!validateTransactionIds(neoTransaction, transactionIds, uniqueTransactionIds)) {
                continue;
            }

            uniqueTransactionIds.addAll(transactionIds);

            Map<String,NeoscanTransactionMetadata> transactionIdToMetadata;
            try {
                transactionIdToMetadata = NeoscanUtils.getTransactionMetadatas(neoTransaction.getFromNeoWallet().getNeoAddress(), transactionIds);
            } catch (IOException e) {
                if(logger.isWarnEnabled()) logger.warn(String.format("Failed lookup of transactions %s on Neoscan", transactionIds), e);
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("Failed lookup of transactions %s on Neoscan! IOException: %s", transactionIds, e.getMessage()));
                continue;
            }

            // bl: make sure we found ALL of the transactions we were looking for
            if(!transactionIdToMetadata.keySet().containsAll(transactionIds)) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("Failed lookup of all transactions %s on Neoscan! Are the transactions recent enough?", transactionIds));
                continue;
            }

            // bl: now let's validate the metadata to make sure it lines up with what we're expecting!
            if(!validateTransactionMetadata(neoTransaction, transactionIdToMetadata, nrveScriptHash)) {
                continue;
            }

            // bl: if we got valid metadata, then put it into our map for processing!
            transactionToMetadata.put(neoTransaction, new ArrayList<>(transactionIdToMetadata.values()));
        }
    }

    private boolean validateToFromAddressesExist(NeoTransaction neoTransaction) {
        boolean hasError = false;
        if(isEmpty(neoTransaction.getToNeoWallet().getNeoAddress())) {
            addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The toNeoWallet address (%s) must be set in NEO Wallets first!", neoTransaction.getToNeoWallet().getNameForDisplay()));
            hasError = true;
        }
        if(isEmpty(neoTransaction.getFromNeoWallet().getNeoAddress())) {
            addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The fromNeoWallet address (%s) must be set in NEO Wallets first!", neoTransaction.getFromNeoWallet().getNameForDisplay()));
            hasError = true;
        }
        return !hasError;
    }

    private boolean validateTransactionIds(NeoTransaction neoTransaction, Set<String> transactionIds, Set<String> uniqueTransactionIds) {
        for (String transactionId : transactionIds) {
            if(!NeoUtils.validateNeoTransactionId(this, "transactionToTransactionId['" + neoTransaction.getOid() + "']", neoTransaction.getOid().toString(), transactionId)) {
                return false;
            }
            if(uniqueTransactionIds.contains(transactionId)) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", "Transaction IDs must be unique!");
                return false;
            }
        }
        return true;
    }

    private boolean validateTransactionMetadata(NeoTransaction neoTransaction, Map<String,NeoscanTransactionMetadata> transactionIdToMetadata, String nrveScriptHash) {
        NrveValue totalAmount = NrveValue.ZERO;
        for (Map.Entry<String, NeoscanTransactionMetadata> entry : transactionIdToMetadata.entrySet()) {
            String transactionId = entry.getKey();
            NeoscanTransactionMetadata metadata = entry.getValue();

            // bl: make sure that the asset, from address, to address, and amounts are all correct!
            if(!isEqual(nrveScriptHash, metadata.getAsset())) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The script hash for transaction %s on Neoscan isn't correct! Found %s but expected %s.", transactionId, metadata.getAsset(), nrveScriptHash));
                return false;
            }

            totalAmount = totalAmount.add(new NrveValue(metadata.getAmount()));

            if(!isEqual(neoTransaction.getToNeoWallet().getNeoAddress(), metadata.getAddressTo())) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The address_to for transaction %s on Neoscan isn't correct! Found %s but expected %s.", transactionId, metadata.getAddressTo(), neoTransaction.getToNeoWallet().getNeoAddress()));
                return false;
            }
            if(!isEqual(neoTransaction.getFromNeoWallet().getNeoAddress(), metadata.getAddressFrom())) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The address_from for transaction %s on Neoscan isn't correct! Found %s but expected %s.", transactionId, metadata.getAddressFrom(), neoTransaction.getFromNeoWallet().getNeoAddress()));
                return false;
            }

            // bl: may as well make sure the transactionIds match, as well
            if(!isEqual(transactionId, metadata.getTransactionId())) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The txid for transaction %s on Neoscan isn't correct! Found %s .", transactionId, metadata.getTransactionId()));
                return false;
            }

            // bl: finally, let's validate the fields that we are actually going to store
            if(metadata.getBlockNumber()<=0) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The block_height for transaction %s on Neoscan isn't valid! Found %s.", transactionId, metadata.getBlockNumber()));
                return false;
            }
            if(metadata.getTransactionDatetime()==null) {
                addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("Failed to find a time for transaction %s on Neoscan!", transactionId));
                return false;
            }
        }

        if(!neoTransaction.getNrveAmount().equals(totalAmount)) {
            addFieldError("transactionToTransactionId['" + neoTransaction.getOid() + "']", String.format("The amount for transactions %s on Neoscan aren't correct! Found %s but expected %s.", transactionIdToMetadata.keySet(), totalAmount.getFormattedWithEightDecimals(), neoTransaction.getNrveAmount().getFormattedWithEightDecimals()));
            return false;
        }

        return true;
    }

    public String input() throws Exception {
        // bl: highlight any transactions that are missing wallet addresses with field errors
        for (NeoTransaction neoTransaction : neoTransactions) {
            validateToFromAddressesExist(neoTransaction);
        }
        // bl: if there are errors, set the formType on the action so that the errors will render!
        if(hasErrors()) {
            setFormType(FORM_TYPE);
        }
        return INPUT;
    }

    @Override
    public String execute() throws Exception {
        for (Map.Entry<NeoTransaction, List<NeoscanTransactionMetadata>> entry : transactionToMetadata.entrySet()) {
            NeoTransaction neoTransaction = entry.getKey();
            List<NeoscanTransactionMetadata> metadatas = entry.getValue();
            for (NeoscanTransactionMetadata metadata : metadatas) {
                neoTransaction.addNeoTransaction(metadata);
            }
        }
        return NetworkResponses.redirectResponse();
    }

    public List<NeoTransaction> getNeoTransactions() {
        return neoTransactions;
    }

    @AfterPrepare
    public Map<NeoTransaction, List<String>> getTransactionToTransactionId() {
        return transactionToTransactionId;
    }

    /**
     * sort NeoTransactions by the order in the enum, and then by the ProratedMonthRevenue month
     */
    private static final Comparator<NeoTransaction> INCOMPLETE_NEO_TRANSACTION_COMPARATOR = (o1, o2) -> {
        int ret = o1.getType().compareTo(o2.getType());
        if(ret!=0) {
            return ret;
        }
        // bl: if there are multiple of the same type, then group them by the from wallet to make doing the transfers easier.
        // bl: first special case is for prorated month revenue, where we want to sort by month
        if(o1.getFromNeoWallet().getType().isProratedMonthRevenue()) {
            ProratedMonthRevenue pmr1 = o1.getFromNeoWallet().getWallet().getProratedMonthRevenue();
            ProratedMonthRevenue pmr2 = o2.getFromNeoWallet().getWallet().getProratedMonthRevenue();
            ret = pmr1.getMonth().compareTo(pmr2.getMonth());
            if(ret!=0) {
                return ret;
            }
        }
        // bl: if there is a wallet transaction type, then sort them in order according to the WalletTransaction datetime
        if(o1.getType().getWalletTransactionType()!=null) {
            ret = o1.getWalletTransaction().getTransactionDatetime().compareTo(o2.getWalletTransaction().getTransactionDatetime());
            if(ret!=0) {
                return ret;
            }
        }
        // bl: final sort discriminator is OID
        return o1.getOid().compareTo(o2.getOid());
    };
}
