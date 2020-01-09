package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.UserElectorateReward;
import org.narrative.network.core.user.User;

/**
 * Date: 2019-05-16
 * Time: 08:35
 *
 * @author jonmark
 */
public class UserElectorateRewardDAO extends RewardTransactionRefDAO<UserElectorateReward> {
    public UserElectorateRewardDAO() {
        super(UserElectorateReward.class);
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        return getGSession().createNamedQuery("userElectorateReward.getCountIncompleteRewardTransactionRefs", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("userElectorateReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.ELECTORATE.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public UserElectorateReward getForUserRewardPeriod(User user, RewardPeriod rewardPeriod) {
        return getUniqueBy(new NameValuePair<>(UserElectorateReward.Fields.user, user), new NameValuePair<>(UserElectorateReward.Fields.period, rewardPeriod));
    }
}
