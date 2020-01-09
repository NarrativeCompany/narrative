package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.network.core.narrative.rewards.NarrativeCompanyReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;

/**
 * Date: 2019-05-16
 * Time: 07:54
 *
 * @author jonmark
 */
public class NarrativeCompanyRewardDAO extends RewardTransactionRefDAO<NarrativeCompanyReward> {
    public NarrativeCompanyRewardDAO() {
        super(NarrativeCompanyReward.class);
    }

    public NarrativeCompanyReward getForPeriod(RewardPeriod period) {
        return getUniqueBy(new NameValuePair<>(NarrativeCompanyReward.Fields.period, period));
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        // bl: period and transaction are both required, so there's no way any values could be invalid.
        return 0;
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("narrativeCompanyReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.NARRATIVE_COMPANY.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }
}
