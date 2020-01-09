package org.narrative.network.customizations.narrative.service.impl.stats;

import org.narrative.common.persistence.ThreadSafeFetchTask;
import org.narrative.common.util.Task;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.service.api.StatsService;
import org.narrative.network.customizations.narrative.service.api.model.NicheStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.StatsOverviewDTO;
import org.narrative.network.customizations.narrative.service.api.model.TopNicheDTO;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.mapper.TopNicheMapper;
import org.narrative.network.customizations.narrative.services.GoogleAnalyticsUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Date: 11/28/18
 * Time: 7:27 AM
 *
 * @author brian
 */
@Service
public class StatsServiceImpl implements StatsService {
    private final StaticMethodWrapper staticMethodWrapper;
    private final TopNicheMapper topNicheMapper;

    public StatsServiceImpl(StaticMethodWrapper staticMethodWrapper, TopNicheMapper topNicheMapper) {
        this.staticMethodWrapper = staticMethodWrapper;
        this.topNicheMapper = topNicheMapper;
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_STATSSERVICE_STATS_OVERVIEW)
    @Override
    public StatsOverviewDTO getStatsOverview() {
        Area narrativeArea = staticMethodWrapper.getAreaContext().getArea();
        long totalMembers = narrativeArea.getAreaStats().getMemberCount();
        Long uniqueVisitorsPast30Days = ThreadSafeFetchTask.doThreadSafeTask(new Task<Long>() {
            @Override
            protected Long doMonitoredTask() {
                return GoogleAnalyticsUtil.getMonthlyUniqueVisitors();
            }
        });
        long activeMembersPast30Days = AreaUser.dao().getActiveMemberCount(narrativeArea, new Timestamp(DateUtils.addDays(new Date(), -30).getTime()));
        long nicheOwners = Niche.dao().getCountOfNicheOwners();
        long approvedNiches = Niche.dao().getCountOfNichesByStatus(NicheStatus.APPROVED_STATUSES);
        long activeNiches = Niche.dao().getCountOfNichesByStatus(Collections.singleton(NicheStatus.ACTIVE));
        NrveValue networkRewardsPaidLastMonth = RewardPeriod.dao().getLatestCompletedRewardPeriodBefore(RewardUtils.nowYearMonth()).getTotalRewardsDisbursed();
        NrveValue allTimeReferralRewards = WalletTransaction.dao().getTransactionSum(WalletTransactionType.REFERRAL_TYPES);
        // bl: no need to round the referral rewards since those are all whole values.
        // bl: this will include all content, which right now should purely be NarrativePost records.
        // if we ever add more content types for Narrative or want to filter by status, this will definitely need to change.
        long totalPosts = Content.dao().getCountAllLiveContent(ContentType.NARRATIVE_POST);
        List<TopNicheDTO> topNiches = topNicheMapper.mapTopNichePairsListToTopNicheList(ChannelContent.dao().getNichesMostPostedTo(10));
        return StatsOverviewDTO.builder()
                .totalMembers(totalMembers)
                .uniqueVisitorsPast30Days(uniqueVisitorsPast30Days)
                .activeMembersPast30Days(activeMembersPast30Days)
                .nicheOwners(nicheOwners)
                .approvedNiches(approvedNiches)
                .activeNiches(activeNiches)
                .networkRewardsPaidLastMonth(new NrveUsdValue(networkRewardsPaidLastMonth))
                .allTimeReferralRewards(new NrveUsdValue(allTimeReferralRewards))
                .totalPosts(totalPosts)
                .topNiches(topNiches)
                // bl: set the timestamp to now so that we know when the data was cached. we'll display
                // this in the UI ("data last updated X minutes ago.")
                .timestamp(new Date())
                .build();
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_STATSSERVICE_NICHE_STATS)
    @Override
    public NicheStatsDTO getNicheStats() {
        long nichesAwaitingApproval = Niche.dao().getCountOfNichesByStatus(Collections.singleton(NicheStatus.SUGGESTED));
        long nichesForSale = Niche.dao().getCountOfNichesByStatus(Collections.singleton(NicheStatus.FOR_SALE));
        return NicheStatsDTO.builder()
                .nichesAwaitingApproval(nichesAwaitingApproval)
                .nichesForSale(nichesForSale)
                .build();
    }
}
