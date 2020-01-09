package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.services.AreaUserList;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.controller.TribunalController;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.TribunalService;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateTribunalIssueInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationTribunalIssueInput;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.mapper.TribunalIssueMapper;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class TribunalServiceImpl implements TribunalService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final UserMapper userMapper;
    private final TribunalIssueMapper tribunalIssueMapper;
    private final StaticMethodWrapper staticMethodWrapper;

    public TribunalServiceImpl(final AreaTaskExecutor areaTaskExecutor, final UserMapper userMapper, final TribunalIssueMapper tribunalIssueMapper, final StaticMethodWrapper staticMethodWrapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.userMapper = userMapper;
        this.tribunalIssueMapper = tribunalIssueMapper;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    public List<UserDTO> getTribunalMembers() {
        final Area area = areaTaskExecutor.getArea();
        final AreaUserList areaUserList = new AreaUserList(area);
        areaUserList.setAreaCircle(area.getAuthZone().getSandboxedCommunitySettings().getCirclesByNarrativeCircleType().get(NarrativeCircleType.TRIBUNAL));
        areaUserList.setSort(AreaUserList.SortField.DISPLAY_NAME);
        areaUserList.setSortAsc(true);
        areaUserList.doSetRowsPerPage(Integer.MAX_VALUE);
        areaUserList.setPage(1);

        final List<AreaUser> areaUsers = areaTaskExecutor.executeGlobalTask(areaUserList);

        return userMapper.mapAreaUserEntityListToUserList(areaUsers);
    }

    @Override
    public PageDataDTO<TribunalIssueDTO> getNichesUnderTribunalReview(Pageable pageRequest, boolean open) {

        // Execute the task to obtain niche information
        ObjectPair<List<TribunalIssue>, Long> tribunalIssues = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ObjectPair<List<TribunalIssue>, Long>>(false) {
            @Override
            protected ObjectPair<List<TribunalIssue>, Long> doMonitoredTask() {
                // Execute the task to obtain niche information
                // Apply data visibility filters for tribunal issues
                long count = Referendum.dao().getCountOfReferendumsByTypeAndStatus(ReferendumType.TRIBUNAL_TYPES, open);
                List<Referendum> referendums = Referendum.dao().getReferendumsByTypeAndStatus(ReferendumType.TRIBUNAL_TYPES, open, pageRequest.getPageNumber()+1, pageRequest.getPageSize());;
                List<TribunalIssue> tribunalIssues = referendums.stream().map(Referendum::getTribunalIssue).collect(Collectors.toList());
                return new ObjectPair<>(tribunalIssues, count);
            }
        });

        //Build a page from the results
        return buildTribunalIssuePage(pageRequest, tribunalIssues);

    }

    @Override
    public PageDataDTO<TribunalIssueDTO> getAppealQueueForCurrentTribunalUser(Pageable pageRequest) {
        // Execute the task to obtain niche information
        ObjectPair<List<TribunalIssue>, Long> tribunalIssues = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ObjectPair<List<TribunalIssue>, Long>>(false) {
            @Override
            protected ObjectPair<List<TribunalIssue>, Long> doMonitoredTask() {
                // jw: only allow tribunal members to access this page!
                getAreaContext().getPrimaryRole().checkCanParticipateInTribunalIssues();

                long count = TribunalIssue.dao().getCountOfOpenIssuesByTypeAndStatusPendingResponse(ReferendumType.TRIBUNAL_TYPES, getAreaContext().getAreaUserRlm());
                List<TribunalIssue> issues = TribunalIssue.dao().getOpenIssuesByTypeAndStatusPendingResponse(ReferendumType.TRIBUNAL_TYPES, getAreaContext().getAreaUserRlm(), pageRequest.getPageNumber()+1, pageRequest.getPageSize());

                return new ObjectPair<>(issues, count);
            }
        });

        //Build a page from the results
        return buildTribunalIssuePage(pageRequest, tribunalIssues);

    }

    private PageDataDTO<TribunalIssueDTO> buildTribunalIssuePage(Pageable pageRequest, ObjectPair<List<TribunalIssue>, Long> pair) {
        List<TribunalIssue> tribunalIssues = pair.getOne();
        List<Referendum> referendums = tribunalIssues.stream().map(TribunalIssue::getReferendum).collect(Collectors.toList());
        Referendum.dao().populateReferendumVotesByCurrentUser(staticMethodWrapper.getAreaContext().getAreaUserRlm(), referendums);
        List<ChannelConsumer> channelConsumers = referendums.stream().map(Referendum::getChannelConsumer).filter(Objects::nonNull).collect(Collectors.toList());
        FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(staticMethodWrapper.networkContext().getUser(), channelConsumers);
        return PageUtil.buildPage(tribunalIssueMapper.mapTribunalIssueListToTribunalIssueDTOList(tribunalIssues), pageRequest, pair.getTwo());
    }

    @Override
    public TribunalIssueDetailDTO getTribunalAppealSummary(OID tribunalIssueId) {

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<TribunalIssueDetailDTO>(false) {
            @Override
            protected TribunalIssueDetailDTO doMonitoredTask() {
                TribunalIssue tribunalIssue = TribunalIssue.dao().getForApiParam(tribunalIssueId, "tribunalIssueId");

                return tribunalIssueMapper.mapTribunalIssueToTribunalIssueDetailDTO(tribunalIssue);
            }
        });
    }

    @Override
    public TribunalIssueDetailDTO createNicheTribunalIssue(OID nicheOid, CreateTribunalIssueInput input) {
        return createTribunalIssue(
                Niche.dao().getForApiParam(nicheOid, TribunalController.NICHE_OID_PARAM),
                input.getType(),
                input.getComment()
        );
    }

    @Override
    public TribunalIssueDetailDTO createPublicationTribunalIssue(OID publicationOid, PublicationTribunalIssueInput input) {
        Publication publication = Publication.dao().getForApiParam(publicationOid, TribunalController.PUBLICATION_OID_PARAM);

        return createTribunalIssue(
                publication,
                TribunalIssueType.RATIFY_PUBLICATION,
                input.getComment()
        );
    }

    private TribunalIssueDetailDTO createTribunalIssue(ChannelConsumer channelConsumer, TribunalIssueType type, String comment) {
        TribunalIssue tribunalIssue = areaTaskExecutor.executeAreaTask(new CreateChannelConsumerTribunalIssueTask(
                channelConsumer,
                type,
                comment
        ));
        return tribunalIssueMapper.mapTribunalIssueToTribunalIssueDetailDTO(tribunalIssue);
    }
}
