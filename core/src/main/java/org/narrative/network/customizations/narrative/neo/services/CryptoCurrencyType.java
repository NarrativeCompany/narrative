package org.narrative.network.customizations.narrative.neo.services;

import org.narrative.common.util.enums.IntegerEnum;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/27/18
 * Time: 1:33 PM
 */
public enum CryptoCurrencyType implements IntegerEnum {
    NRVE(0, 2956, "NRVE"),
    BITCOIN(1, 1, "BTC"),
    ETHEREUM(2, 1027, "ETH"),
    NEO(3, 1376, "NEO");

    private final int id;
    private final Integer coinMarketCapId;
    private final String symbol;

    CryptoCurrencyType(int id, Integer coinMarketCapId, String symbol) {
        this.id = id;
        this.coinMarketCapId = coinMarketCapId;
        this.symbol = symbol;
    }

    @Override
    public int getId() {
        return id;
    }

    public Integer getCoinMarketCapId() {
        return coinMarketCapId;
    }

    public boolean isTrackedOnCoinMarketCap() {
        return coinMarketCapId != null;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isNrve() {
        return this == NRVE;
    }

    public BigDecimal getCurrentUsdValue() {
        return CoinMarketCapApiRequests.getCurrentUsdValue(this);
    }
}