package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class})
public interface ReferendumVoteMapper {
    @Mapping(source = "commentReply.oid", target = ReferendumVoteDTO.Fields.commentOid)
    @Mapping(source = "commentReply.body", target = ReferendumVoteDTO.Fields.comment)
    @Mapping(source = "votePointsFormattedForApi", target = ReferendumVoteDTO.Fields.votePoints)
    ReferendumVoteDTO mapReferendumVoteEntityToReferendumVoteDTO(ReferendumVote referendumVoteEntity);

    List<ReferendumVoteDTO> mapReferendumVoteEntitiesToReferendumVoteDTOs(List<ReferendumVote> referendumVoteEntities);
}
