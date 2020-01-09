package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.referendum.ReferendumVoteInputDTO;
import org.narrative.network.customizations.narrative.service.api.ReferendumService;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVoteGroupingDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumVotesDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/referendums")
@Validated
public class ReferendumController {

    public static final String REFERENDUM_OID_PARAM = "referendumOid";
    private static final String REFERENDUM_OID_PARAMSPEC = "{" + REFERENDUM_OID_PARAM + "}";

    private final ReferendumService referendumService;

    public ReferendumController(ReferendumService referendumService) {
        this.referendumService = referendumService;
    }

    @GetMapping("/ballot-box")
    public PageDataDTO<ReferendumDTO> findReferendums(@PageableDefault(size = 25) Pageable pageRequest) {
        return referendumService.findReferendums(pageRequest);
    }

    @GetMapping("/" + REFERENDUM_OID_PARAMSPEC)
    public ReferendumDTO getReferendumById(@PathVariable(REFERENDUM_OID_PARAM) OID referendumId) {
        return referendumService.getReferendumById(referendumId);
    }

    @GetMapping("/" + REFERENDUM_OID_PARAMSPEC + "/votes")
    public ReferendumVotesDTO getReferendumVotesById(@PathVariable(REFERENDUM_OID_PARAM) OID referendumId) {
        return referendumService.getReferendumVotes(referendumId);
    }

    @GetMapping("/" + REFERENDUM_OID_PARAMSPEC + "/votes/{votedFor}")
    public ReferendumVoteGroupingDTO getReferendumVotesForType(
            @PathVariable(REFERENDUM_OID_PARAM) OID referendumOid,
            @PathVariable("votedFor") @NotNull Boolean votedFor,
            @RequestParam @NotNull String lastVoterDisplayName,
            @RequestParam @NotNull String lastVoterUsername
    ) {
        return referendumService.getReferendumVotesForType(referendumOid, votedFor, lastVoterDisplayName, lastVoterUsername);
    }

    @PostMapping("/" + REFERENDUM_OID_PARAMSPEC + "/votes")
    public ReferendumDTO vote(@PathVariable(REFERENDUM_OID_PARAM) OID referendumOid, @Valid @RequestBody ReferendumVoteInputDTO referendumVoteRequest) {
        return referendumService.voteOnReferendum(referendumOid, referendumVoteRequest);
    }

    @PutMapping("/" + REFERENDUM_OID_PARAMSPEC + "/extend")
    public ReferendumDTO extendReferendum(@PathVariable(REFERENDUM_OID_PARAM) OID referendumOid) {
        return referendumService.extendReferendum(referendumOid);
    }

    @PutMapping("/" + REFERENDUM_OID_PARAMSPEC + "/end")
    public ReferendumDTO endReferendum(@PathVariable(REFERENDUM_OID_PARAM) OID referendumOid) {
        return referendumService.endReferendum(referendumOid);
    }
}
