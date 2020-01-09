package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.ElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineeDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineesDTO;

import java.time.Instant;

/**
 * Date: 11/13/18
 * Time: 3:43 PM
 *
 * @author jonmark
 */
public interface ElectionService {
    /**
     * Find confirmed election nominees for a specific Election ordered by confirmation datetime.
     *
     * @param electionOid     The OID of the election to get nominees for.
     * @param confirmedBefore Optional confirmation point in time to cutoff nominees with.
     * @param count           The number of nominees to return.
     * @return Matching {@link ElectionNomineesDTO}
     */
    ElectionNomineesDTO findElectionNominees(OID electionOid, Instant confirmedBefore, int count);

    /**
     * Nominate the current user to the specified election.
     *
     * @param electionOid       The OID of the election to nominate the current user for.
     * @param personalStatement The optional personalStatement to include with the nomination.
     * @return The {@link ElectionNomineeDTO} corresponding to the current users nomination after the process finished.
     */
    public ElectionDetailDTO nominateCurrentUser(OID electionOid, String personalStatement);

    /**
     * Nominate the current user to the specified election.
     *
     * @param electionOid The OID of the election to nominate the current user for.
     * @return The {@link ElectionNomineeDTO} corresponding to the current users nomination after the process finished.
     */
    public ElectionDetailDTO withdrawNominationForCurrentUser(OID electionOid);
}
