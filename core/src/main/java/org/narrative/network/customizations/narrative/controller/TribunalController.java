package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.tribunal.CreateTribunalIssueInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.tribunal.PublicationTribunalIssueInputDTO;
import org.narrative.network.customizations.narrative.service.api.TribunalService;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/tribunal")
@Validated
public class TribunalController {
    public static final String NICHE_OID_PARAM = "nicheOid";
    public static final String PUBLICATION_OID_PARAM = "publicationOid";

    private final TribunalService tribunalService;

    public TribunalController(final TribunalService tribunalService) {
        this.tribunalService = tribunalService;
    }

    @GetMapping("/members")
    public List<UserDTO> getTribunalMembers() {
        return tribunalService.getTribunalMembers();
    }

    @GetMapping("/niches-under-review")
    public PageDataDTO<TribunalIssueDTO> getNichesUnderTribunalReview(@PageableDefault(size = 50) Pageable pageRequest) {
        return tribunalService.getNichesUnderTribunalReview(pageRequest, true);
    }

    @GetMapping("/niches-completed-review")
    public PageDataDTO<TribunalIssueDTO> getNichesWithCompletedTribunalReview(@PageableDefault(size = 50) Pageable pageRequest) {
        return tribunalService.getNichesUnderTribunalReview(pageRequest, false);
    }

    @GetMapping("/my-queue")
    public PageDataDTO<TribunalIssueDTO> getAppealQueueForCurrentTribunalUser(@PageableDefault(size = 50) Pageable pageRequest) {
        return tribunalService.getAppealQueueForCurrentTribunalUser(pageRequest);
    }

    @PostMapping("/appeals/niches/{" + NICHE_OID_PARAM + "}")
    public TribunalIssueDetailDTO createNicheTribunalIssue(@PathVariable(NICHE_OID_PARAM) OID nicheOid, @Valid @RequestBody CreateTribunalIssueInputDTO createNicheTribunalIssueInput) {
        return tribunalService.createNicheTribunalIssue(nicheOid, createNicheTribunalIssueInput);
    }

    @PostMapping("/appeals/publications/{" + PUBLICATION_OID_PARAM + "}")
    public TribunalIssueDetailDTO createPublicationTribunalIssue(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @Valid @RequestBody PublicationTribunalIssueInputDTO input) {
        return tribunalService.createPublicationTribunalIssue(publicationOid, input);
    }

    @GetMapping("/appeals/{id}")
    public TribunalIssueDetailDTO getTribunalAppealSummary(@PathVariable("id") OID tribunalIssueId) {
        return tribunalService.getTribunalAppealSummary(tribunalIssueId);
    }
}
