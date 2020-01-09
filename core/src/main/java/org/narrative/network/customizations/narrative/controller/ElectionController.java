package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.controller.postbody.election.ElectionNominationInputDTO;
import org.narrative.network.customizations.narrative.service.api.ElectionService;
import org.narrative.network.customizations.narrative.service.api.model.ElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineeDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineesDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import java.time.Instant;

/**
 * Date: 11/13/18
 * Time: 3:40 PM
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/elections")
@Validated
public class ElectionController {
    public static final String ELECTION_OID_PARAM = "electionOid";
    public static final String ELECTION_OID_PARAMSPEC = "{" + ELECTION_OID_PARAM + "}";

    public static final String USER_OID_PARAM = "userOid";
    public static final String USER_OID_PARAMSPEC = "{" + USER_OID_PARAM + "}";

    private final ElectionService electionService;
    private final NarrativeProperties narrativeProperties;

    public ElectionController(final ElectionService electionService, NarrativeProperties narrativeProperties) {
        this.electionService = electionService;
        this.narrativeProperties = narrativeProperties;
    }

    /**
     * Find confirmed Election Nominees for a specified election ordered by confirmation datetime.
     *
     * @param electionOid     The Election to fetch Nominees for.
     * @param confirmedBefore Optional cutoff for nominees by confirmation datetime.
     * @param count           The number of nominees to return
     * @return {@link ElectionNomineesDTO}
     */
    @GetMapping("/" + ELECTION_OID_PARAMSPEC + "/nominees")
    public ElectionNomineesDTO findElectionNominees(
            @PathVariable(ELECTION_OID_PARAM) OID electionOid,
            @RequestParam(required = false) Instant confirmedBefore,
            @Positive @RequestParam(defaultValue = "50") int count
    ) {
        count = Math.min(count, narrativeProperties.getSpring().getMvc().getMaxPageSize());

        return electionService.findElectionNominees(electionOid, confirmedBefore, count);
    }

    /**
     * Invite a different user to participate in the specified election.
     *
     * @param electionOid The Election to invite the other user to.
     * @param userOid The User to invite to the election
     * @return {@link ElectionNomineeDTO}
     */
    @PutMapping("/" + ELECTION_OID_PARAMSPEC + "/nominees/" + USER_OID_PARAMSPEC + "/invitations")
    public ElectionNomineeDTO nominateOtherToElection(@PathVariable(ELECTION_OID_PARAM) OID electionOid, @PathVariable(USER_OID_PARAM) OID userOid) {
        throw UnexpectedError.getRuntimeException("This will be implemented as part of #1482");
    }

    /**
     * Nominate the current user to the specified election.
     *
     * @param electionOid The Election to nominate the current user to.
     * @return {@link ElectionNomineeDTO}
     */
    @PutMapping("/" + ELECTION_OID_PARAMSPEC + "/nominees/current-user")
    public ElectionDetailDTO nominateCurrentUser(@PathVariable(ELECTION_OID_PARAM) OID electionOid, @Valid @RequestBody ElectionNominationInputDTO nominationInput) {
        return electionService.nominateCurrentUser(electionOid, nominationInput == null ? null : nominationInput.getPersonalStatement());
    }

    /**
     * Revoke the current user's nomination to the specified election.
     *
     * @param electionOid The Election to revoke the current users nomination from.
     * @return {@link ElectionNomineeDTO}
     */
    @DeleteMapping("/" + ELECTION_OID_PARAMSPEC + "/nominees/current-user")
    public ElectionDetailDTO withdrawNominationForCurrentUser(@PathVariable(ELECTION_OID_PARAM) OID electionOid) {
        return electionService.withdrawNominationForCurrentUser(electionOid);
    }
}
