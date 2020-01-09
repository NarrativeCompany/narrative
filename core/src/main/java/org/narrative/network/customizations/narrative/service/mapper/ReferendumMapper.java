package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteGroupingDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVotesDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class, PublicationMapper.class, ReferendumVoteMapper.class, ChannelMapper.class})
public interface ReferendumMapper {
    int VOTES_PER_PAGE = 100;

    /**
     * Map from {@link Referendum} entity to {@link ReferendumDTO}.
     *
     * @param referendumEntity Incoming Referendum entity.
     * @return mapped Referendum DTO.
     */
    @Mapping(source = "votePointsForFormattedForApi", target = ReferendumDTO.Fields.votePointsFor)
    @Mapping(source = "votePointsAgainstFormattedForApi", target = ReferendumDTO.Fields.votePointsAgainst)
    ReferendumDTO mapReferendumEntityToReferendumDTO(Referendum referendumEntity);

    /**
     * Map a {@link List} of {@link Referendum} to a {@link List} of {@link ReferendumDTO}.
     *
     * @param referendumList Incoming list of Referendum entities
     * @return List of Referendum DTOs.
     */
    List<ReferendumDTO> mapReferendumEntityListToReferendumDTOList(List<Referendum> referendumList);

    @Mapping(source = "votePointsForFormattedForApi", target = ReferendumVotesDTO.Fields.votePointsFor)
    @Mapping(source = "votePointsAgainstFormattedForApi", target = ReferendumVotesDTO.Fields.votePointsAgainst)
    @Mapping(expression = "java(mapReferendumVotesToGroupingDTO(true, referendum.getRecentVotesFor(), VOTES_PER_PAGE))", target = "recentVotesFor")
    @Mapping(expression = "java(mapReferendumVotesToGroupingDTO(false, referendum.getRecentVotesAgainst(), VOTES_PER_PAGE))", target = "recentVotesAgainst")
    @Mapping(expression = "java(!referendum.getType().isTribunalReferendum() ? null : mapReferendumVotesToGroupingDTO(null, referendum.getTribunalMembersYetToVote(), null))", target = "tribunalMembersYetToVote")
    ReferendumVotesDTO mapReferendumToReferendumVotesDTO(Referendum referendum);

    @Mapping(source = "votes", target = "items")
    @Mapping(source = "votedFor", target = "votedFor")
    @Mapping(expression = "java(count != null && votes.size() == count)", target = "hasMoreItems")
    @Mapping(expression = "java(votes.isEmpty() ? null : votes.get(votes.size()-1).getVoter().getDisplayNameResolved())", target = "lastVoterDisplayName")
    @Mapping(expression = "java(votes.isEmpty() ? null : votes.get(votes.size()-1).getVoter().getUser().getUsername())", target = "lastVoterUsername")
    // jw: because MapStruct uses the first item to determine what type of mapping this is, I need to place a single object as the first element.
    //     If the votes are first, it will error out because it expects to be creating a collection of objects.
    ReferendumVoteGroupingDTO mapReferendumVotesToGroupingDTO(Boolean votedFor, List<ReferendumVote> votes, Integer count);
}
