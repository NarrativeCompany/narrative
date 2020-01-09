package org.narrative.network.customizations.narrative.neo.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.JSONMap;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2019-06-12
 * Time: 08:13
 *
 * @author brian
 */
public class NeoscanUtils {
    private static final NetworkLogger logger = new NetworkLogger(NeoscanUtils.class);

    private static final String NEOSCAN_MAINNET_API_BASE_URL = "https://api.neoscan.io/api/main_net";
    private static final String NEOSCAN_TESTNET_API_BASE_URL = "https://api.neoscan.io/api/test_net";

    private static final String BASE_URL = "https://neoscan.io/";
    private static final String TRANSACTION_BASE_URL = BASE_URL + "transaction/";

    public static final String NEOSCAN_ADDRESS_TRANSACTIONS_PATH = "/v1/get_address_abstracts/";

    public static String getNeoscanApiBaseUrl() {
        return NetworkRegistry.getInstance().isUsesNeoMainNet() ? NEOSCAN_MAINNET_API_BASE_URL : NEOSCAN_TESTNET_API_BASE_URL;
    }

    public static Map<String,NeoscanTransactionMetadata> getTransactionMetadatas(String neoAddress, Collection<String> transactionIds) throws IOException {
        // bl: looking up a transaction by ID works, but it doesn't include the amount or asset information in it.
        // so, we'll look through at most the first 20 pages of transactions looking for our transaction
        Map<String,NeoscanTransactionMetadata> ret = new HashMap<>();
        // bl: neoscan currently returns 15 results per page, so we need to make sure we scan at least the minimum
        // number of pages based on the count of transactions supplied.
        // bl: add a 5 page buffer beyond the count of transactionIds supplied
        int pagesOfTransactionIds = (int)Math.ceil((double)transactionIds.size()/15);
        // bl: scan up to 20 pages, at a minimum.
        int maxPagesToScan = Math.max(20, pagesOfTransactionIds + 5);
        // bl: Neoscan includes two transactions for each redemption: one for NRVE and one for GAS. as a result,
        // we need to double the count.
        maxPagesToScan *= 2;
        Set<String> transactionIdsToFind = new HashSet<>(transactionIds);
        for (int i = 0; i < maxPagesToScan; i++) {
            List<NeoscanTransactionMetadata> transactions = getTransactionsPage(neoAddress, i);
            // bl: if we ever fail to get results, then just bail
            if(transactions==null) {
                break;
            }
            for (NeoscanTransactionMetadata transaction : transactions) {
                String transactionId = transaction.getTransactionId();
                if(transactionIdsToFind.contains(transactionId)) {
                    // add it to our map to return
                    ret.put(transactionId, transaction);
                    // remove it from our set since we don't need to look for it anymore!
                    transactionIdsToFind.remove(transactionId);
                    // bl: stop once we find all of the transactions!
                    if(transactionIdsToFind.isEmpty()) {
                        return ret;
                    }
                }
            }
        }

        // bl: at this point, we didn't find all of the transactions, but return what we did find
        return ret;
    }

    public static List<NeoscanTransactionMetadata> getTransactionsPage(String neoAddress, int page) throws IOException {
        String url = getNeoscanApiBaseUrl() + NEOSCAN_ADDRESS_TRANSACTIONS_PATH + neoAddress + "/" + page;
        String json = getResponseBodyFromUrl(url);
        JSONMap jsonMap = JSONMap.getJsonMap(json);
        if(jsonMap==null) {
            if(logger.isWarnEnabled()) logger.warn("Failed to identify JSON from URL/" + url + " response/" + json);
            return null;
        }
        List<JSONMap> entries = jsonMap.getList("entries");
        if(entries==null) {
            if(logger.isWarnEnabled()) logger.warn("No entries found for URL/" + url);
            return Collections.emptyList();
        }
        List<NeoscanTransactionMetadata> ret = new ArrayList<>(entries.size());
        for (JSONMap entryMap : entries) {
            long txTime = entryMap.getLong("time");
            String asset = entryMap.getString("asset");
            String addressTo = entryMap.getString("address_to");
            String addressFrom = entryMap.getString("address_from");
            BigDecimal amount = new BigDecimal(entryMap.getString("amount"));
            long blockNumber = entryMap.getLong("block_height");
            String txId = entryMap.getString("txid");
            ret.add(new NeoscanTransactionMetadata(txId, Instant.ofEpochSecond(txTime), blockNumber, asset, addressFrom, addressTo, amount));
        }
        return ret;
    }

    private static String getResponseBodyFromUrl(String url) throws IOException {
        int timeout = 10* IPDateUtil.SECOND_IN_MS;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        // bl: opting for this approach instead of a simple IOUtils.toString() or other option
        // since apparently Neoscan doesn't like Java's default User-Agent and rejects it with a 403 / Forbidden.
        HttpClient client = HttpClientBuilder.create()
                .setUserAgent("curl/7.54.0")
                .setDefaultRequestConfig(config)
                .build();
        HttpGet getRequest = new HttpGet(url);
        HttpResponse httpResponse = client.execute(getRequest);
        if(httpResponse.getStatusLine().getStatusCode()!= HttpServletResponse.SC_OK) {
            throw new IOException("Server returned HTTP response code: " + httpResponse.getStatusLine().getStatusCode() + " for URL: " + url);
        }
        return EntityUtils.toString(httpResponse.getEntity());
    }

    public static String getTransactionUrl(String transactionId) {
        return TRANSACTION_BASE_URL + transactionId;
    }
}
