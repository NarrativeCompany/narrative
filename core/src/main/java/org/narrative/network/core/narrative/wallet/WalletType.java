package org.narrative.network.core.narrative.wallet;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 2019-05-14
 * Time: 15:27
 *
 * @author jonmark
 */
public enum WalletType implements IntegerEnum {
    USER(0, false, false, NeoWalletType.MEMBER_CREDITS)
    ,REWARD_PERIOD(1, true, false, NeoWalletType.MONTHLY_REWARDS)
    ,TOKEN_MINT(2, false, true, NeoWalletType.TOKEN_MINT)
    ,NICHE_MONTH_REVENUE(3, true, false, NeoWalletType.PRORATED_NICHE_REVENUE)
    ,PUBLICATION_MONTH_REVENUE(4, true, false, NeoWalletType.PRORATED_PUBLICATION_REVENUE)
    ;

    private final int id;
    private final boolean supportsNegativeBalances;
    private final boolean singleton;
    private final NeoWalletType neoWalletType;

    WalletType(int id, boolean supportsNegativeBalances, boolean singleton, NeoWalletType neoWalletType) {
        this.id = id;
        this.supportsNegativeBalances = supportsNegativeBalances;
        this.singleton = singleton;
        this.neoWalletType = neoWalletType;
    }

    private static final Map<NeoWalletType,WalletType> NEO_WALLET_TYPE_TO_WALLET_TYPE;

    static {
        Map<NeoWalletType,WalletType> map = new HashMap<>();
        for (WalletType type : values()) {
            assert !type.isSingleton() || type.neoWalletType.isSingleton() : "Should never have a NeoWalletType that is a non-singleton with a WalletType that is a singleton!";
            map.put(type.neoWalletType, type);
        }
        NEO_WALLET_TYPE_TO_WALLET_TYPE = Collections.unmodifiableMap(map);
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isProratedMonthRevenue() {
        return ProratedRevenueType.WALLET_TYPE_TO_PRORATED_REVENUE_TYPE.containsKey(this);
    }

    public boolean isSupportsNegativeBalances() {
        return supportsNegativeBalances;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public boolean isAllowsSharedNeoWallets() {
        return isUser();
    }

    public NeoWalletType getNeoWalletType() {
        return neoWalletType;
    }

    public boolean isSameCardinalityAsNeoWalletType() {
        return isSingleton() == neoWalletType.isSingleton();
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isRewardPeriod() {
        return this == REWARD_PERIOD;
    }

    public static WalletType getWalletTypeForNeoWalletType(NeoWalletType neoWalletType) {
        return NEO_WALLET_TYPE_TO_WALLET_TYPE.get(neoWalletType);
    }
}