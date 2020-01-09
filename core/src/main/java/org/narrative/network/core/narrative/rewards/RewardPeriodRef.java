package org.narrative.network.core.narrative.rewards;

/**
 * Date: 2019-05-15
 * Time: 20:52
 *
 * @author jonmark
 */
public interface RewardPeriodRef {
    String FIELD__PERIOD__NAME = "period";
    String FIELD__PERIOD__COLUMN = FIELD__PERIOD__NAME+"_"+ RewardPeriod.FIELD__OID__NAME;

    RewardPeriod getPeriod();
    void setPeriod(RewardPeriod period);
}
