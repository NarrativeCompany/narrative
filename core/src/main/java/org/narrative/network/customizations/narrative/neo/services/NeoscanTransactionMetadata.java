package org.narrative.network.customizations.narrative.neo.services;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Date: 2019-06-12
 * Time: 08:17
 *
 * @author brian
 */
@Data
public class NeoscanTransactionMetadata {
    private final String transactionId;
    private final Instant transactionDatetime;
    private final long blockNumber;
    private final String asset;
    private final String addressFrom;
    private final String addressTo;
    private final BigDecimal amount;
}
