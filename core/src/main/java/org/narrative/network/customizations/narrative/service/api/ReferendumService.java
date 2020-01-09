package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.referendum.ReferendumVoteInputDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteGroupingDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVotesDTO;
import org.springframework.data.domain.Pageable;

public interface ReferendumService {

    PageDataDTO<ReferendumDTO> findReferendums(Pageable pageRequest);

    ReferendumDTO getReferendumById(OID referendumOid);

    ReferendumVotesDTO getReferendumVotes(OID referendumOid);

    ReferendumVoteGroupingDTO getReferendumVotesForType(OID referendumOid, boolean votedFor, String lastVoterDisplayName, String lastVoterUsername);

    ReferendumDTO voteOnReferendum(OID referendumOid, ReferendumVoteInputDTO referendumVoteInputDTO);

    ReferendumDTO extendReferendum(OID referendumOid);

    ReferendumDTO endReferendum(OID referendumOid);
}
