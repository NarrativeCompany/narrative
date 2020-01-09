package org.narrative.network.customizations.narrative.service.impl.referendum;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.controller.ReferendumController;
import org.narrative.network.customizations.narrative.controller.postbody.referendum.ReferendumVoteInputDTO;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.referendum.services.EndReferendumTask;
import org.narrative.network.customizations.narrative.niches.referendum.services.RescheduleReferendumTask;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ReferendumService;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteGroupingDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVotesDTO;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.mapper.ReferendumMapper;
import org.narrative.network.customizations.narrative.service.mapper.ReferendumVoteMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

@Service
@Transactional
public class ReferendumServiceImpl implements ReferendumService {

    private final AreaTaskExecutor areaTaskExecutor;
    private final ReferendumMapper referendumMapper;
    private final ReferendumVoteMapper referendumVoteMapper;

    public ReferendumServiceImpl(
            final AreaTaskExecutor areaTaskExecutor,
            final ReferendumMapper referendumMapper,
            final ReferendumVoteMapper referendumVoteMapper
    ) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.referendumMapper = referendumMapper;
        this.referendumVoteMapper = referendumVoteMapper;
    }

    @Override
    public PageDataDTO<ReferendumDTO> findReferendums(Pageable pageRequest) {

        Pair<Long, List<ReferendumDTO>> referendumPair = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Pair<Long, List<ReferendumDTO>>>() {

            @Override
            public boolean isForceWritable() {
                return false;
            }

            @Override
            protected Pair<Long, List<ReferendumDTO>> doMonitoredTask() {
                List<Referendum> result = Referendum.dao().getReferendumsByTypeAndStatus(ReferendumType.NICHE_TYPES, true, pageRequest.getPageNumber() + 1, pageRequest.getPageSize());
                long totalCount = Referendum.dao().getCountOfReferendumsByTypeAndStatus(ReferendumType.NICHE_TYPES, true);

                // jw: if the requester is a logged in user, let's populate the followedByCurrentUser flag on all niches.
                if (getNetworkContext().isLoggedInUser()) {
                    List<ChannelConsumer> channelConsumers = Referendum.dao().getChannelConsumersFromReferendums(result);

                    FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(getNetworkContext().getUser(), channelConsumers);
                }

                // bl: also need to populate the current user's votes for the list of Referendums. do this for guests, too
                Referendum.dao().populateReferendumVotesByCurrentUser(getAreaContext().getAreaUserRlm(), result);

                return new ImmutablePair<>(totalCount, referendumMapper.mapReferendumEntityListToReferendumDTOList(result));
            }
        });

        return PageUtil.buildPage(referendumPair.getRight(), pageRequest, referendumPair.getLeft());
    }

    @Override
    public ReferendumDTO getReferendumById(OID referendumOid) {
        Referendum referendum = Referendum.dao().getForApiParam(referendumOid, ReferendumController.REFERENDUM_OID_PARAM);
        return referendumMapper.mapReferendumEntityToReferendumDTO(referendum);
    }

    @Override
    public ReferendumVotesDTO getReferendumVotes(OID referendumOid) {
        Referendum referendum = Referendum.dao().getForApiParam(referendumOid, ReferendumController.REFERENDUM_OID_PARAM);
        return referendumMapper.mapReferendumToReferendumVotesDTO(referendum);
    }

    @Override
    public ReferendumVoteGroupingDTO getReferendumVotesForType(OID referendumOid, boolean votedFor, String lastVoterDisplayName, String lastVoterUsername) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<ReferendumVoteGroupingDTO>(false) {
            @Override
            protected ReferendumVoteGroupingDTO doMonitoredTask() {
                Referendum referendum = Referendum.dao().getForApiParam(referendumOid, ReferendumController.REFERENDUM_OID_PARAM);

                List<ReferendumVote> votes = ReferendumVote.dao().getRecentVotes(
                        referendum,
                        votedFor,
                        lastVoterDisplayName,
                        lastVoterUsername,
                        ReferendumMapper.VOTES_PER_PAGE
                );

                User lastVoter = votes.isEmpty()
                        ? null
                        : votes.get(votes.size()-1).getVoter().getUser();

                // Map the entity results into niche DTOs
                List<ReferendumVoteDTO> voteDtos = referendumVoteMapper.mapReferendumVoteEntitiesToReferendumVoteDTOs(votes);

                return ReferendumVoteGroupingDTO.builder()
                        .items(voteDtos)
                        .hasMoreItems(votes.size() == ReferendumMapper.VOTES_PER_PAGE)
                        .votedFor(votedFor)
                        .lastVoterDisplayName(exists(lastVoter) ? lastVoter.getDisplayName() : null)
                        .lastVoterUsername(exists(lastVoter) ? lastVoter.getUsername() : null)
                        .build();
            }
        });
    }

    @Override
    public ReferendumDTO voteOnReferendum(OID referendumOid, ReferendumVoteInputDTO referendumVoteInputDTO) {
        VoteOnReferendumTask voteOnReferendumTask = new VoteOnReferendumTask(referendumOid, referendumVoteInputDTO);
        ReferendumVote referendumVote = areaTaskExecutor.executeAreaTask(voteOnReferendumTask);
        // bl: if a comment was made, then make sure we return it on the ReferendumVoteDTO
        referendumVote.getReferendum().setupReferendumVoteComments(Collections.singleton(referendumVote));
        return referendumMapper.mapReferendumEntityToReferendumDTO(referendumVote.getReferendum());
    }

    @Override
    public ReferendumDTO extendReferendum(OID referendumOid) {
        Referendum referendum = validateReferendumTestAction(referendumOid);
        areaTaskExecutor.executeAreaTask(new RescheduleReferendumTask(referendum));
        return referendumMapper.mapReferendumEntityToReferendumDTO(referendum);
    }

    @Override
    public ReferendumDTO endReferendum(OID referendumOid) {
        Referendum referendum = validateReferendumTestAction(referendumOid);
        // jw: let's go ahead and set the end datetime to now, since we are ending it now.
        referendum.setEndDatetime(now());
        areaTaskExecutor.executeAreaTask(new EndReferendumTask(referendum));
        return referendumMapper.mapReferendumEntityToReferendumDTO(referendum);
    }

    private static Referendum validateReferendumTestAction(OID referendumOid) {
        if (NetworkRegistry.getInstance().isProductionServer()) {
            throw UnexpectedError.getRuntimeException("Should only ever use Referendum test endpoints on Dev or QA servers!");
        }
        Referendum referendum = Referendum.dao().getForApiParam(referendumOid, ReferendumController.REFERENDUM_OID_PARAM);
        if (!referendum.isOpen()) {
            throw UnexpectedError.getRuntimeException("The specified Referendum must be open to complete a test action!");
        }
        return referendum;
    }
}

