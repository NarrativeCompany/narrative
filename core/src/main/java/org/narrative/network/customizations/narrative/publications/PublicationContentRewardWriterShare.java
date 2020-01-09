package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 2019-08-26
 * Time: 14:45
 *
 * @author jonmark
 */
public enum PublicationContentRewardWriterShare implements IntegerEnum {
    ONE_HUNDRED_PERCENT(0, 100),
    NINETY_PERCENT(1, 90),
    SEVENTY_FIVE_PERCENT(2, 75),
    FIFTY_PERCENT(3, 50),
    TWENTY_FIVE_PERCENT(4, 25),
    TEN_PERCENT(5, 10),
    ZERO_PERCENT(6, 0),
    ;

    private final int id;
    private final int writerPercentage;

    PublicationContentRewardWriterShare(int id, int writerPercentage) {
        this.id = id;
        this.writerPercentage = writerPercentage;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getWriterPercentage() {
        return writerPercentage;
    }

    public boolean isOneHundredPercent() {
        return this==ONE_HUNDRED_PERCENT;
    }
}