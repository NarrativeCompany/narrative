package org.narrative.network.core.narrative.wallet;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-05-16
 * Time: 15:18
 *
 * @author jonmark
 */
public enum WalletTransactionStatus implements IntegerEnum {
    COMPLETED(0)
    ,PENDING_FIAT_ADJUSTMENT(1)
    ,PENDING(2)
    ,PROCESSING(3)
    ;

    private final int id;

    WalletTransactionStatus(int id) {
        this.id = id;
    }

    public static final Set<WalletTransactionStatus> BULK_PROCESSED_STATUSES = Collections.unmodifiableSet(EnumSet.of(COMPLETED, PENDING, PROCESSING));
    public static final Set<WalletTransactionStatus> FIAT_ADJUSTMENT_STATUSES = Collections.unmodifiableSet(EnumSet.of(COMPLETED, PENDING_FIAT_ADJUSTMENT));
    public static final Set<WalletTransactionStatus> COMPLETED_STATUS_ONLY = Collections.unmodifiableSet(EnumSet.of(COMPLETED));

    @Override
    public int getId() {
        return id;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isPendingFiatAdjustment() {
        return this == PENDING_FIAT_ADJUSTMENT;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isProcessing() {
        return this == PROCESSING;
    }
}