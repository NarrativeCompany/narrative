package org.narrative.network.core.user;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 2019-06-26
 * Time: 13:32
 *
 * @author jonmark
 */
public enum UserRedemptionStatus implements IntegerEnum {
    // jw: This enum is closely tied to the React enhanced version (userRedemptionStatus.ts) of this enum, particularly
    //     since the walletUpdatable and supportsRedemption flags are duplicated there. It's vital that changes made here
    //     are reflected in the enhanced typescript version as well.
    WALLET_UNSPECIFIED(0, true, false)
    ,WALLET_IN_WAITING_PERIOD(1, true, false)
    ,HAS_PENDING_REDEMPTION(2, false, false)
    ,REDEMPTION_AVAILABLE(4, true, true)
    ;

    private final int id;
    private final boolean walletUpdatable;
    private final boolean supportsRedemption;

    UserRedemptionStatus(int id, boolean walletUpdatable, boolean supportsRedemption) {
        this.id = id;
        this.walletUpdatable = walletUpdatable;
        this.supportsRedemption = supportsRedemption;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isWalletUpdatable() {
        return walletUpdatable;
    }

    public boolean isSupportsRedemption() {
        return supportsRedemption;
    }

    public boolean isWalletInWaitingPeriod() {
        return this == WALLET_IN_WAITING_PERIOD;
    }
}