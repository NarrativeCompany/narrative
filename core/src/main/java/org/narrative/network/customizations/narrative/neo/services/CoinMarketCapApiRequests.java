package org.narrative.network.customizations.narrative.neo.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.system.NetworkRegistry;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/28/18
 * Time: 9:04 AM
 */
public class CoinMarketCapApiRequests {
    private CoinMarketCapApiRequests() {
        throw UnexpectedError.getRuntimeException("Should never instantiate this class!");
    }

    private static final String PRODUCTION_API_URL = "https://pro-api.coinmarketcap.com/v1";
    private static final String SANDBOX_API_URL = "https://sandbox-api.coinmarketcap.com/v1";
    // API documentation: https://coinmarketcap.com/api/documentation/v1/#operation/getV1CryptocurrencyQuotesLatest
    private static final String QUOTE_PATH = "/cryptocurrency/quotes/latest";
    private static final String SYMBOL_PARAM = "symbol";

    private static final String COINMARKETCAP_API_KEY_HEADER = "X-CMC_PRO_API_KEY";

    private static final String USD_QUOTES_KEY = "USD";

    public static final int MAX_PRICE_DECIMAL_DIGITS = 8;

    public static BigDecimal getCurrentUsdValue(CryptoCurrencyType type) {
        assert type.isTrackedOnCoinMarketCap() : "Should only ever fetch the USD value for CryptoCurrencyTypes that support this API.";
        String json;
        try {
            String baseUrl = (NetworkRegistry.getInstance().isUsesNeoMainNet() ? PRODUCTION_API_URL : SANDBOX_API_URL) + QUOTE_PATH;
            String url = IPHTMLUtil.getParametersAsURL(baseUrl, Collections.singletonMap(SYMBOL_PARAM, type.getSymbol()));
            json = getResponseBodyFromUrl(url);
        } catch (IOException ioe) {
            throw UnexpectedError.getRuntimeException("Failed making API Request for type " + type + "!", ioe);
        }

        CmcApiResult result = IPStringUtil.parseFromJson(json, CmcApiResult.class);
        if (result == null) {
            throw UnexpectedError.getRuntimeException("Failed parsing result from API Request for " + type + "!");
        }

        if (result.getStatus() == null) {
            throw UnexpectedError.getRuntimeException("Failed parsing result status from API Request for " + type + "!");
        }

        if (result.getStatus().getError_code()!=0) {
            throw UnexpectedError.getRuntimeException("received error making API Request for " + type + "! error_code/" + result.getStatus().getError_code() + " error_message/" + result.getStatus().getError_message());
        }

        if (result.getData() == null) {
            throw UnexpectedError.getRuntimeException("No 'data' was provided from API Request for " + type + "!");
        }

        CmcCoinData coinData = result.getData().get(type.getSymbol());
        if (coinData==null) {
            throw UnexpectedError.getRuntimeException("No 'data' was provided from API Request for " + type + "!");
        }

        if(coinData.getId()!=type.getCoinMarketCapId()) {
            throw UnexpectedError.getRuntimeException("CMC ID mismatch! Expected/" + type.getCoinMarketCapId() + " but found/" + coinData.getId());
        }

        if(!isEqual(coinData.getSymbol(), type.getSymbol())) {
            throw UnexpectedError.getRuntimeException("CMC symbol mismatch! Expected/" + type.getSymbol() + " but found/" + coinData.getSymbol());
        }

        CmcApiCurrencyQuote usdQuote = coinData.getQuote().get(USD_QUOTES_KEY);
        if (usdQuote == null) {
            throw UnexpectedError.getRuntimeException("No USD quote was provided from API Request for " + type + "!");
        }

        if (usdQuote.getPrice() == null) {
            throw UnexpectedError.getRuntimeException("No USD value was provided from API Request for " + type + "!");
        }

        // bl: we'll store at most 8 decimals of precision
        // bl: always strip off trailing zeros
        return usdQuote.getPrice().setScale(MAX_PRICE_DECIMAL_DIGITS, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private static String getResponseBodyFromUrl(String url) throws IOException {
        int timeout = 5*IPDateUtil.SECOND_IN_MS;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        HttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader(COINMARKETCAP_API_KEY_HEADER, StaticConfig.getBean(NarrativeProperties.class).getSecrets().getCoinMarketCapApiKey());
        HttpResponse httpResponse = client.execute(getRequest);
        if(httpResponse.getStatusLine().getStatusCode()!= HttpServletResponse.SC_OK) {
            throw new IOException("Server returned HTTP response code: " + httpResponse.getStatusLine().getStatusCode() + " for URL: " + url);
        }
        return EntityUtils.toString(httpResponse.getEntity());
    }

    public static class CmcApiResult {
        private Map<String, CmcCoinData> data;
        private CmcApiStatus status;

        public Map<String, CmcCoinData> getData() {
            return data;
        }

        public void setData(Map<String, CmcCoinData> data) {
            this.data = data;
        }

        public CmcApiStatus getStatus() {
            return status;
        }

        public void setStatus(CmcApiStatus status) {
            this.status = status;
        }
    }

    public static class CmcCoinData {
        private int id;
        private String symbol;
        private Map<String, CmcApiCurrencyQuote> quote;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Map<String, CmcApiCurrencyQuote> getQuote() {
            return quote;
        }

        public void setQuote(Map<String, CmcApiCurrencyQuote> quote) {
            this.quote = quote;
        }
    }

    public static class CmcApiCurrencyQuote {
        private BigDecimal price;

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    public static class CmcApiStatus {
        private int error_code;
        private String error_message;

        public int getError_code() {
            return error_code;
        }

        public void setError_code(int error_code) {
            this.error_code = error_code;
        }

        public String getError_message() {
            return error_message;
        }

        public void setError_message(String error_message) {
            this.error_message = error_message;
        }
    }
}
