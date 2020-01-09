package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import javax.persistence.LockModeType;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-16
 * Time: 15:25
 *
 * @author jonmark
 */
public class ProratedMonthRevenueDAO extends GlobalDAOImpl<ProratedMonthRevenue, OID> {
    public ProratedMonthRevenueDAO() {
        super(ProratedMonthRevenue.class);
    }

    public ProratedMonthRevenue getForYearMonthAndType(YearMonth yearMonth, ProratedRevenueType revenueType) {
        return getUniqueBy(
                new NameValuePair<>(ProratedMonthRevenue.Fields.month, yearMonth)
                , new NameValuePair<>(ProratedMonthRevenue.Fields.type, revenueType)
        );
    }

    public Map<ProratedRevenueType,ProratedMonthRevenue> getForYearMonthByType(YearMonth yearMonth) {
        List<ProratedMonthRevenue> revenues = getAllBy(new NameValuePair<>(ProratedMonthRevenue.Fields.month, yearMonth));
        return revenues.stream().collect(Collectors.toMap(ProratedMonthRevenue::getType, Function.identity()));
    }

    public List<ProratedMonthRevenue> getAllWithAvailableCaptures(RewardPeriod rewardPeriod, LockModeType lockModeType) {
        return getGSession().createNamedQuery("proratedMonthRevenue.getAllWithAvailableCaptures", ProratedMonthRevenue.class)
                .setParameter("maxCaptures", RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR)
                .setParameter("throughRewardPeriodMonth", rewardPeriod.getPeriod())
                .setLockMode(lockModeType)
                .list();
    }

    public ProratedMonthRevenue getForWallet(Wallet wallet, LockModeType lockModeType) {
        assert wallet.getType().isProratedMonthRevenue() : "Should only look up a ProratedMonthRevenue for a wallet corresponding to ProratedMonthRevenue, not/" + wallet.getType();
        // bl: calling this via a query so that we can use unique result. it'll blow up if there's more than one (which there should not be).
        // note that this is not an indexed lookup, but there shouldn't be too many rows in this table, so i'm not concerned
        // about the performance of that.
        return getGSession().createNamedQuery("proratedMonthRevenue.getForWallet", ProratedMonthRevenue.class)
                .setParameter("wallet", wallet)
                .setLockMode(lockModeType)
                .uniqueResult();
    }

    public List<Wallet> getWalletsForTypeAndYearMonths(Collection<ProratedRevenueType> types, List<YearMonth> yearMonths) {
        return getGSession().createNamedQuery("proratedMonthRevenue.getWalletsForTypeAndYearMonths", Wallet.class)
                .setParameterList("types", types)
                .setParameterList("yearMonths", yearMonths)
                .list();
    }
}
