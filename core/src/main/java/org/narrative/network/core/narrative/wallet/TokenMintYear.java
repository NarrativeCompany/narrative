package org.narrative.network.core.narrative.wallet;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.customizations.narrative.NrveValue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2019-05-14
 * Time: 15:08
 *
 * @author jonmark
 */
public enum TokenMintYear implements IntegerEnum {
    YEAR_1(1, new NrveValue(BigDecimal.valueOf(12000000)))
    ,YEAR_2(2, new NrveValue(BigDecimal.valueOf(11000000)))
    ,YEAR_3(3, new NrveValue(BigDecimal.valueOf(10000000)))
    ,YEAR_4(4, new NrveValue(BigDecimal.valueOf(9000000)))
    ,YEAR_5(5, new NrveValue(BigDecimal.valueOf(8000000)))
    ,YEAR_6(6, new NrveValue(BigDecimal.valueOf(7000000)))
    ,YEAR_7(7, new NrveValue(BigDecimal.valueOf(6000000)))
    ,YEAR_8(8, new NrveValue(BigDecimal.valueOf(5000000)))
    ,YEAR_9(9, new NrveValue(BigDecimal.valueOf(3500000)))
    ,YEAR_10(10, new NrveValue(BigDecimal.valueOf(2500000)))
    ,YEAR_11(11, new NrveValue(BigDecimal.valueOf(1500000)))
    ,YEAR_12(12, new NrveValue(BigDecimal.valueOf(900000)))
    ,YEAR_13(13, new NrveValue(BigDecimal.valueOf(700000)))
    ,YEAR_14(14, new NrveValue(BigDecimal.valueOf(500000)))
    ,YEAR_15(15, new NrveValue(BigDecimal.valueOf(400000)))
    ;

    private static final Map<TokenMintYear, TokenMintYear> NEXT_YEAR_LOOKUP;
    static {
        Map<TokenMintYear, TokenMintYear> lookup = new HashMap<>();
        Set<NrveValue> tokenAmounts = new HashSet<>();
        TokenMintYear previousMintYear = null;
        for (TokenMintYear mintYear : values()) {
            if (previousMintYear!=null) {
                lookup.put(previousMintYear, mintYear);
            }
            previousMintYear = mintYear;
            if(tokenAmounts.contains(mintYear.getTotalTokens())) {
                throw UnexpectedError.getRuntimeException("Every TokenMintYear should have a different number of tokens! Otherwise, RecordTokenMintTransactionForYear will fail in validating unique transactions!");
            }
            tokenAmounts.add(mintYear.getTotalTokens());
        }


        NEXT_YEAR_LOOKUP = Collections.unmodifiableMap(lookup);
    }

    private final int id;
    private final NrveValue totalTokens;
    private final NrveValue tokensPerMonth;

    TokenMintYear(int id, NrveValue totalTokens) {
        this.id = id;
        this.totalTokens = totalTokens;
        this.tokensPerMonth = RewardUtils.calculatePerCaptureValue(totalTokens, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR);
    }

    @Override
    public int getId() {
        return id;
    }

    public TokenMintYear getNextYear() {
        return NEXT_YEAR_LOOKUP.get(this);
    }

    public NrveValue getTotalTokens() {
        return totalTokens;
    }

    public NrveValue getTokensForCapture(int capture) {
        return RewardUtils.calculateCaptureValue(
                totalTokens,
                tokensPerMonth,
                capture,
                RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR
        );
    }
}