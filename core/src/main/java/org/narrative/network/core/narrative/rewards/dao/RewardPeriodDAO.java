package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.query.Query;

import java.time.YearMonth;
import java.util.List;

/**
 * Date: 2019-05-13
 * Time: 13:56
 *
 * @author jonmark
 */
public class RewardPeriodDAO extends GlobalDAOImpl<RewardPeriod, OID> {
    public RewardPeriodDAO() {
        super(RewardPeriod.class);
    }

    public RewardPeriod getForYearMonth(YearMonth yearMonth) {
        return getUniqueByWithCache(new NameValuePair<>(RewardPeriod.Fields.period, yearMonth));
    }

    public OID getOidForYearMonth(YearMonth yearMonth) {
        return getGSession().createNamedQuery("rewardPeriod.getOidForYearMonth", OID.class)
                .setParameter("period", yearMonth)
                .uniqueResult();
    }

    public RewardPeriod getLatestRewardPeriod() {
        return getGSession().createNamedQuery("rewardPeriod.getLatestRewardPeriod", RewardPeriod.class)
                .setMaxResults(1)
                .uniqueResult();
    }

    public RewardPeriod getOldestIncompleteRewardPeriodBefore(YearMonth before) {
        return getGSession().createNamedQuery("rewardPeriod.getOldestIncompleteRewardPeriodBefore", RewardPeriod.class)
                .setParameter("before", before)
                .setMaxResults(1)
                .uniqueResult();
    }

    public List<RewardPeriod> getAllIncompleteRewardPeriods() {
        return getGSession().createNamedQuery("rewardPeriod.getAllIncompleteRewardPeriods", RewardPeriod.class)
                .list();
    }

    public RewardPeriod getLatestRewardPeriodBefore(YearMonth before) {
        return getGSession().createNamedQuery("rewardPeriod.getLatestRewardPeriodBefore", RewardPeriod.class)
                .setParameter("before", before)
                .setMaxResults(1)
                .uniqueResult();
    }

    public RewardPeriod getLatestCompletedRewardPeriodBefore(YearMonth before) {
        return getGSession().createNamedQuery("rewardPeriod.getLatestCompletedRewardPeriodBefore", RewardPeriod.class)
                .setParameter("before", before)
                .setMaxResults(1)
                .uniqueResult();
    }

    public List<RewardPeriod> getAllCompletedPeriods() {
        return getAllCompletedPeriods(null);
    }

    public List<RewardPeriod> getAllCompletedPeriods(Integer limit) {
        Query query = getGSession().createNamedQuery("rewardPeriod.getAllCompletedPeriods", RewardPeriod.class)
                .setCacheable(true);
        if(limit!=null) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public NrveValue getAllTimeRewardsDisbursed() {
        NrveValue value = getGSession().createNamedQuery("rewardPeriod.getAllTimeRewardsDisbursed", NrveValue.class)
                .setCacheable(true)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }

    public List<RewardPeriod> getRewardPeriodsForUser(User user) {
        return getGSession().createNamedQuery("rewardPeriod.getRewardPeriodsForUser", RewardPeriod.class)
                .setParameter("toWallet", user.getWallet())
                .list();
    }
}
