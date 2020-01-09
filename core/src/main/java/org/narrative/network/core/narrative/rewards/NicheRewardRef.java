package org.narrative.network.core.narrative.rewards;

/**
 * Date: 2019-05-15
 * Time: 21:07
 *
 * @author jonmark
 */
public interface NicheRewardRef {
    String FIELD__NICHE_REWARD__NAME = "nicheReward";
    String FIELD__NICHE_REWARD__COLUMN = FIELD__NICHE_REWARD__NAME+"_"+NicheReward.FIELD__OID__NAME;

    NicheReward getNicheReward();
    void setNicheReward(NicheReward nicheReward);
}
