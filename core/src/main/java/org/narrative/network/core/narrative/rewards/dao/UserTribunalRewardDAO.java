package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.UserTribunalReward;
import org.narrative.network.core.user.User;

/**
 * Date: 2019-05-16
 * Time: 08:49
 *
 * @author jonmark
 */
public class UserTribunalRewardDAO extends RewardTransactionRefDAO<UserTribunalReward> {
    public UserTribunalRewardDAO() {
        super(UserTribunalReward.class);
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        // bl: period and transaction are both required, so there's no way any values could be invalid.
        return 0;
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("userTribunalReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.TRIBUNAL.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public UserTribunalReward getForUserRewardPeriod(User user, RewardPeriod rewardPeriod) {
        return getUniqueBy(new NameValuePair<>(UserTribunalReward.Fields.user, user), new NameValuePair<>(UserTribunalReward.Fields.period, rewardPeriod));
    }
}
