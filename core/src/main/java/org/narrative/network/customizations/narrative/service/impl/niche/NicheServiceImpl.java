package org.narrative.network.customizations.narrative.service.impl.niche;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.InvalidParamError;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.ElectionController;
import org.narrative.network.customizations.narrative.controller.NicheController;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.ElectionStatus;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheOfInterest;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheList;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.NicheService;
import org.narrative.network.customizations.narrative.service.api.RewardsService;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorSlotsDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheProfileDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheRewardPeriodStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardPostDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateNicheRequest;
import org.narrative.network.customizations.narrative.service.api.model.input.NicheInputBase;
import org.narrative.network.customizations.narrative.service.api.model.input.SimilarNicheSearchRequest;
import org.narrative.network.customizations.narrative.service.api.services.ParseObjectFromUnknownIdTask;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.impl.tribunal.CreateNicheEditTribunalIssueTask;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;
import org.narrative.network.customizations.narrative.service.mapper.NicheMapper;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.ReferendumMapper;
import org.narrative.network.customizations.narrative.service.mapper.RewardPeriodMapper;
import org.narrative.network.customizations.narrative.service.mapper.TribunalIssueMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

@Service
public class NicheServiceImpl implements NicheService {
    public static final String NICHE_PARAM = "niche";

    private final AreaTaskExecutor areaTaskExecutor;
    private final NicheMapper nicheMapper;
    private final NicheDerivativeMapper nicheDerivativeMapper;
    private final TribunalIssueMapper tribunalIssueMapper;
    private final UserMapper userMapper;
    private final StaticMethodWrapper staticMethodWrapper;
    private final ReferendumMapper referendumMapper;
    private final PostMapper postMapper;
    private final RewardPeriodMapper rewardPeriodMapper;
    private final RewardsService rewardsService;
    private final ContentStreamService contentStreamService;

    public NicheServiceImpl(AreaTaskExecutor areaTaskExecutor,
                            NicheMapper nicheMapper,
                            NicheDerivativeMapper nicheDerivativeMapper,
                            UserMapper userMapper,
                            TribunalIssueMapper tribunalIssueMapper,
                            StaticMethodWrapper staticMethodWrapper,
                            ReferendumMapper referendumMapper,
                            PostMapper postMapper,
                            RewardPeriodMapper rewardPeriodMapper,
                            RewardsService rewardsService,
                            ContentStreamService contentStreamService) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.nicheMapper = nicheMapper;
        this.nicheDerivativeMapper = nicheDerivativeMapper;
        this.userMapper = userMapper;
        this.tribunalIssueMapper = tribunalIssueMapper;
        this.staticMethodWrapper = staticMethodWrapper;
        this.referendumMapper = referendumMapper;
        this.postMapper = postMapper;
        this.rewardPeriodMapper = rewardPeriodMapper;
        this.rewardsService = rewardsService;
        this.contentStreamService = contentStreamService;
    }

    @Override
    public PageDataDTO<NicheDTO> findNiches(NicheList nicheListCriteria, Pageable pageRequest) {
        //Mutate the predicate criteria with paging criteria
        PageUtil.mutateCriteriaListWithPagingCriteria(nicheListCriteria, pageRequest);

        // Execute the task to obtain niche information
        List<NicheDTO> res = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<NicheDTO>>(false) {
            @Override
            protected List<NicheDTO> doMonitoredTask() {
                // Execute the task to obtain niche information
                List<Niche> res = areaTaskExecutor.executeAreaTask(nicheListCriteria);

                FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(getNetworkContext().getUser(), res);

                //Map the entity results into niche DTOs
                return nicheDerivativeMapper.mapNicheEntityListToNicheList(res);
            }
        });

        //Build a page from the results
        return PageUtil.buildPage(res, pageRequest, nicheListCriteria.getCount());
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_NICHESERVICE_TRENDING_NICHE_OIDS)
    @Override
    public List<OID> getTrendingNicheOids(int count) {
        return TrendingContent.dao().getTrendingNicheOids(count);
    }

    @Override
    public NicheDetailDTO findNicheByUnknownId(String nicheId) {
        return nicheDerivativeMapper.mapNicheEntityToNicheDetail(getNicheFromUnknownId(nicheId));
    }

    @Override
    public NicheProfileDTO findNicheProfileByUnknownId(String nicheId) {
        Niche niche = getNicheFromUnknownId(nicheId);

        // jw:todo:#1542: update with appropriate security check
        if (!exists(niche.getOwnerResolved()) || !niche.getOwnerResolved().getUser().isCurrentUserThisUser()) {
            throw new AccessViolation();
        }

        return nicheDerivativeMapper.mapNicheToNicheProfileDto(niche);
    }

    @Override
    public NicheModeratorSlotsDTO findNicheModeratorSlotsByUnknownId(String nicheId) {
        Niche niche = getNicheFromUnknownId(nicheId);

        return nicheDerivativeMapper.mapNicheToNicheModeratorSlotsDto(niche);
    }

    @Override
    public NicheModeratorSlotsDTO updateNicheModeratorSlots(OID nicheOid, int moderatorSlots) {
        Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);
        checkNicheOwnerPermission(niche);
        // bl: for now, literally all we are doing is setting the moderatorSlots, so let's not over-complicate this.
        // set it on the niche
        niche.setModeratorSlots(moderatorSlots);
        // set it on the election
        niche.getActiveModeratorElection().getElection().setAvailableSlots(moderatorSlots);
        return nicheDerivativeMapper.mapNicheToNicheModeratorSlotsDto(niche);
    }

    private void checkNicheOwnerPermission(Niche niche) {
        // jw:todo:#1542: update with appropriate security check
        if (!exists(niche.getOwnerResolved()) || !niche.getOwnerResolved().getUser().isCurrentUserThisUser()) {
            throw new AccessViolation();
        }
        // the niche must be active or else you can't manage it
        if (!niche.getStatus().isActive()) {
            throw new AccessViolation();
        }
    }

    @Override
    public Niche getNicheFromUnknownId(String nicheId) {
        Niche niche = areaTaskExecutor.executeAreaTask(new ParseObjectFromUnknownIdTask<Niche>(nicheId, NicheController.NICHE_ID_PARAM) {
            @Override
            protected Niche getFromOid(OID oid) {
                return Niche.dao().get(oid);
            }

            @Override
            protected Niche getFromPrettyUrlString(String prettyUrlString) {
                return Niche.dao().getForPrettyURLString(getAreaContext().getPortfolio(), prettyUrlString);
            }
        });

        // if we couldn't get a niche, we have to throw an error
        if (!exists(niche)) {
            throw new InvalidParamError(NicheController.NICHE_ID_PARAM, nicheId);
        }

        return niche;
    }

    @Override
    public ReferendumDTO createNiche(CreateNicheRequest createNicheRequest) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ReferendumDTO>(false) {
            @Override
            protected ReferendumDTO doMonitoredTask() {
                Referendum referendum = getAreaContext().doAreaTask(new CreateNicheTask(createNicheRequest));
                return referendumMapper.mapReferendumEntityToReferendumDTO(referendum);
            }
        });
    }

    public TribunalIssueDetailDTO submitNicheUpdateRequest(Niche niche, NicheInputBase nicheInput) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<TribunalIssueDetailDTO>() {
            @Override
            protected TribunalIssueDetailDTO doMonitoredTask() {
                // create the TribunalIssue
                TribunalIssue issue = getAreaContext().doAreaTask(new CreateNicheEditTribunalIssueTask(niche, nicheInput));

                return tribunalIssueMapper.mapTribunalIssueToTribunalIssueDetailDTO(issue);
            }
        });
    }

    @Override
    public List<NicheDTO> findSimilarNiches(SimilarNicheSearchRequest similarNicheSearchRequest) {
        // Execute the task to obtain niche information
        List<Niche> similarNiches = areaTaskExecutor.executeAreaTask(new FindSimilarNichesForNewNicheTask(similarNicheSearchRequest));
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), similarNiches);
        return nicheDerivativeMapper.mapNicheEntityListToNicheList(similarNiches);
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_NICHESERVICE_SIMILAR_NICHES_BY_OID)
    public List<OID> findSimilarNicheOids(OID nicheOid) {
        Niche niche = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Niche>(false) {
            @Override
            protected Niche doMonitoredTask() {
                return Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);
            }
        });

        // Execute the task to obtain niche information
        List<Niche> niches = areaTaskExecutor.executeAreaTask(new FindSimilarNichesTask(niche.getName(), niche.getDescription(), niche));
        return Niche.dao().getIdsFromObjects(niches);
    }

    @Override
    public List<NicheDTO> getNicheDTOsForNicheOids(List<OID> nicheOids) {
        List<Niche> niches = Niche.dao().getObjectsFromIDsWithCache(nicheOids);
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), niches);
        return nicheDerivativeMapper.mapNicheEntityListToNicheList(niches);
    }

    @Override
    public List<NicheDTO> getNichesOfInterest() {
        List<NicheOfInterest> nicheOfInterestList = NicheOfInterest.dao().getNichesOfInterest();
        List<NicheDTO> niches = new ArrayList<>(nicheOfInterestList.size());

        for (NicheOfInterest nicheOfInterest : nicheOfInterestList) {
            niches.add(nicheMapper.mapNicheEntityToNiche(nicheOfInterest.getNiche()));
        }

        return niches;
    }

    public PageDataDTO<UserDTO> getRandomNicheFollowers(OID nicheOid, int limit) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<PageDataDTO<UserDTO>>(false) {
            @Override
            protected PageDataDTO<UserDTO> doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);
                List<UserDTO> followers = userMapper.mapUserEntityListToUserList(FollowedChannel.dao().getRandomFollowers(niche.getChannel(), limit));
                // bl: only do the query if we know we need to based on the random followers returned.
                int followerCount = followers.size() < limit ? followers.size() : FollowedChannel.dao().getFollowerCount(niche.getChannel());
                // build a PageRequest for this being page #1, with the limit number of items per page
                return PageUtil.buildPage(followers, PageRequest.of(0, limit), followerCount);
            }
        });
    }

    @Override
    public List<RewardPeriodDTO> getNicheRewardPeriods(OID nicheOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<RewardPeriodDTO>>(false) {
            @Override
            protected List<RewardPeriodDTO> doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);

                List<RewardPeriod> rewardPeriods = NicheReward.dao().getNicheRewardPeriods(niche);

                return rewardPeriodMapper.mapRewardPeriodEntityListToRewardPeriodDTOList(rewardPeriods);
            }
        });
    }

    @Override
    public ScalarResultDTO<NrveUsdValue> getNicheAllTimeRewards(OID nicheOid) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ScalarResultDTO<NrveUsdValue>>(false) {
            @Override
            protected ScalarResultDTO<NrveUsdValue> doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);

                NrveValue allTimeRewards = NicheOwnerReward.dao().getNicheAllTimeRewards(niche);
                return ScalarResultDTO.<NrveUsdValue>builder().value(new NrveUsdValue(allTimeRewards)).build();
            }
        });
    }

    @Override
    public NicheRewardPeriodStatsDTO getNicheRewardPeriodRewards(OID nicheOid, String yearMonthStr) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<NicheRewardPeriodStatsDTO>(false) {
            @Override
            protected NicheRewardPeriodStatsDTO doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);

                RewardPeriod rewardPeriod = rewardsService.getRewardPeriodFromParam(yearMonthStr, false);

                NicheReward nicheReward = NicheReward.dao().getForNichePeriod(niche, rewardPeriod);
                NrveValue totalOwnerReward = NrveValue.ZERO;
                NrveValue totalModeratorReward = NrveValue.ZERO;
                long totalQualifyingPosts = 0;
                if(exists(nicheReward)) {
                    NicheOwnerReward nicheOwnerReward = NicheOwnerReward.dao().getForNicheReward(nicheReward);
                    if(exists(nicheOwnerReward)) {
                        totalOwnerReward = nicheOwnerReward.getTransaction().getNrveAmount();
                    }
                    totalModeratorReward = NicheModeratorReward.dao().getTransactionSumForNicheReward(nicheReward);
                    totalQualifyingPosts = NicheContentReward.dao().getCountForNicheReward(nicheReward);
                }

                return NicheRewardPeriodStatsDTO.builder()
                        .rewardPeriodRange(rewardPeriod.getRewardYearMonth().getRewardPeriodRange())
                        .totalOwnerReward(new NrveUsdValue(totalOwnerReward))
                        .totalModeratorReward(new NrveUsdValue(totalModeratorReward))
                        .totalQualifyingPosts(totalQualifyingPosts)
                        .build();
            }
        });
    }

    @Override
    public List<RewardLeaderboardPostDTO> getNichePostLeaderboard(OID nicheOid, String yearMonthStr, int limit) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<RewardLeaderboardPostDTO>>(false) {
            @Override
            protected List<RewardLeaderboardPostDTO> doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);

                RewardPeriod rewardPeriod = rewardsService.getRewardPeriodFromParam(yearMonthStr, false);

                List<ObjectPair<OID, NrveValue>> contentOidAndReward;
                // bl: if we got a reward period, we need to get the leaderboard for that period
                if(exists(rewardPeriod)) {
                    NicheReward nicheReward = NicheReward.dao().getForNichePeriod(niche, rewardPeriod);
                    // bl: if there is no NicheReward, then there aren't any results to include
                    if(!exists(nicheReward)) {
                        contentOidAndReward = Collections.emptyList();
                    } else {
                        contentOidAndReward = NicheContentReward.dao().getTopPostsForNicheReward(nicheReward, limit);
                    }
                } else {
                    // if we didn't get a reward period, then we need the all-time niche leaderboard
                    contentOidAndReward = NicheContentReward.dao().getTopPostsAllTimeForNiche(niche, limit);
                }

                Set<OID> contentOids = ObjectPair.getAllUniqueOnes(contentOidAndReward);
                Map<OID, Content> contentOidToContent = Content.dao().getIDToObjectsFromIDs(contentOids);
                // bl: avoid ObjectNotFoundExceptions for deleted content by removing Content that doesn't exist from the map
                contentOidToContent.values().removeIf(((Predicate<Content>)CoreUtils::exists).negate());
                contentStreamService.populateFollowedChannelsForContentList(getNetworkContext().getUser(), contentOidToContent.values());
                List<ObjectTriplet<OID,Content,NrveValue>> results = new ArrayList<>(contentOidAndReward.size());
                for (ObjectPair<OID, NrveValue> pair : contentOidAndReward) {
                    OID postOid = pair.getOne();
                    results.add(new ObjectTriplet<>(postOid, contentOidToContent.get(postOid), pair.getTwo()));
                }
                return postMapper.mapContentRewardObjectPairListToRewardLeaderboardPostDTOList(results);
            }
        });
    }

    @Override
    public List<RewardLeaderboardUserDTO> getNicheCreatorLeaderboard(OID nicheOid, String yearMonthStr, int limit) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<List<RewardLeaderboardUserDTO>>(false) {
            @Override
            protected List<RewardLeaderboardUserDTO> doMonitoredTask() {
                Niche niche = Niche.dao().getForApiParam(nicheOid, NicheController.NICHE_OID_PARAM);

                RewardPeriod rewardPeriod = rewardsService.getRewardPeriodFromParam(yearMonthStr, false);

                List<ObjectPair<OID, NrveValue>> userOidAndReward;
                // bl: if we got a reward period, we need to get the leaderboard for that period
                if(exists(rewardPeriod)) {
                    NicheReward nicheReward = NicheReward.dao().getForNichePeriod(niche, rewardPeriod);
                    // bl: if there is no NicheReward, then there aren't any results to include
                    if(!exists(nicheReward)) {
                        userOidAndReward = Collections.emptyList();
                    } else {
                        userOidAndReward = NicheContentReward.dao().getTopCreatorsForNicheReward(nicheReward, limit);
                    }
                } else {
                    // if we didn't get a reward period, then we need the all-time niche leaderboard
                    userOidAndReward = NicheContentReward.dao().getTopCreatorsAllTimeForNiche(niche, limit);
                }

                Set<OID> userOids = ObjectPair.getAllUniqueOnes(userOidAndReward);
                Map<OID, User> userOidToUser = User.dao().getIDToObjectsFromIDs(userOids);
                List<ObjectPair<User,NrveValue>> results = new ArrayList<>(userOidAndReward.size());
                for (ObjectPair<OID, NrveValue> pair : userOidAndReward) {
                    results.add(new ObjectPair<>(userOidToUser.get(pair.getOne()), pair.getTwo()));
                }
                return userMapper.mapUserRewardObjectPairListToRewardLeaderboardUserDTOList(results);
            }
        });
    }

    @Override
    public PageDataDTO<NicheModeratorElectionDTO> findNicheModeratorElections(Pageable pageRequest) {
        Pair<List<NicheModeratorElection>, Long> listAndCount = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Pair<List<NicheModeratorElection>, Long>>(false) {
            @Override
            protected Pair<List<NicheModeratorElection>, Long> doMonitoredTask() {
                long totalElections = NicheModeratorElection.dao().getElectionCount(ElectionStatus.NOMINATING);
                List<NicheModeratorElection> moderatorElections = NicheModeratorElection.dao().getElections(
                        ElectionStatus.NOMINATING,
                        // jw: this seems dumb, but let's increment the page index by 1 so it becomes an actual page number.
                        //     The getElections method will turn it back into a index, but this should make it simpler when
                        //     we switch all incoming data to page numbers, moving away from indexes.
                        pageRequest.getPageNumber() + 1,
                        pageRequest.getPageSize()
                );

                // jw: now that we have the list, let's pre-populate the election nominee counts.
                List<Election> elections = moderatorElections.stream().map(NicheModeratorElection::getElection).collect(Collectors.toList());
                ElectionNominee.dao().populateElectionNomineeCounts(elections);

                // jw: Let's just populate the Niche.followedByCurrentUser to null since that data should not be needed
                if (getNetworkContext().getPrimaryRole().isRegisteredUser()) {
                    for (NicheModeratorElection moderatorElection : moderatorElections) {
                        moderatorElection.getNiche().getChannel().setFollowedByCurrentUser(false);
                    }
                }

                // jw: we are finally ready to return the results
                return Pair.of(moderatorElections, totalElections);
            }
        });

        // jw: now that we have the data, let's create the PageData from it.
        return PageUtil.buildPage(
                nicheDerivativeMapper.mapNicheModeratorElectionListToDtoList(listAndCount.getLeft()),
                pageRequest,
                listAndCount.getRight()
        );
    }

    @Override
    public NicheModeratorElectionDetailDTO findNicheModeratorElectionByOid(OID nicheModeratorElectionOid) {
        NicheModeratorElection moderatorElection = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<NicheModeratorElection>(false) {
            @Override
            protected NicheModeratorElection doMonitoredTask() {
                return NicheModeratorElection.dao().getForApiParam(nicheModeratorElectionOid, ElectionController.ELECTION_OID_PARAM);
            }
        });

        return nicheDerivativeMapper.mapNicheModeratorElectionToDetailDto(moderatorElection);
    }
}
