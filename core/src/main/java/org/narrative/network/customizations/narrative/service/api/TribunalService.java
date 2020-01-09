package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateTribunalIssueInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationTribunalIssueInput;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TribunalService {
    /**
     * @return {@link List} of {@link UserDTO}
     */
    List<UserDTO> getTribunalMembers();

    PageDataDTO<TribunalIssueDTO> getNichesUnderTribunalReview(Pageable pageRequest, boolean open);

    PageDataDTO<TribunalIssueDTO> getAppealQueueForCurrentTribunalUser(Pageable pageRequest);

    TribunalIssueDetailDTO getTribunalAppealSummary(OID tribunalIssueId);

    TribunalIssueDetailDTO createNicheTribunalIssue(OID nicheOid, CreateTribunalIssueInput createTribunalIssueInput);

    TribunalIssueDetailDTO createPublicationTribunalIssue(OID publicationOid, PublicationTribunalIssueInput input);
}