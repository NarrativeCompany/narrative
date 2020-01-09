package org.narrative.network.customizations.narrative.elections.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/12/18
 * Time: 10:33 AM
 *
 * @author jonmark
 */
public class ElectionNomineeDAO extends GlobalDAOImpl<ElectionNominee, OID> {
    public ElectionNomineeDAO() {
        super(ElectionNominee.class);
    }

    public Map<OID, Long> getElectionNomineeCounts(Collection<Election> elections) {
        if (isEmptyOrNull(elections)) {
            return Collections.emptyMap();
        }

        List<ObjectPair<OID, Long>> results = getGSession().getNamedQuery("electionNominee.getElectionNomineeCounts")
                .setParameterList("elections", elections)
                .setParameter("confirmedStatus", NomineeStatus.CONFIRMED)
                .list();

        return ObjectPair.getAsMap(results);
    }

    public void populateElectionNomineeCounts(Collection<Election> elections) {
        // jw: if there are no elections, then there is nothing to do.
        if (isEmptyOrNull(elections)) {
            return;
        }

        // jw: next, let's get the count of nominees for each election. Note: this will only contain elections that
        //     actually have nominees, so we will need to handle that below.
        Map<OID, Long> countLookup = getElectionNomineeCounts(elections);

        // jw: finally, let's iterate over all elections and set the nominee counts. As mentioned above, we need to iterate
        //     over all elections, since the 'results' only contains elections with nominees
        for (Election election : elections) {
            Long nomineeCount = countLookup.get(election.getOid());

            election.setNomineeCount(nomineeCount == null ? 0 : nomineeCount.intValue());
        }
    }

    public ElectionNominee getForUser(Election election, User user) {
        assert exists(election) : "Should always provide an election.";
        assert exists(user) : "Should always provide a user.";

        return getUniqueBy(
                new NameValuePair<>(ElectionNominee.FIELD__ELECTION__NAME, election),
                new NameValuePair<>(ElectionNominee.FIELD__NOMINEE__NAME, user)
        );
    }

    public List<ElectionNominee> getConfirmedForElection(Election election, User excludeUser, Instant confirmedBefore, int count) {
        assert exists(election) : "Should always provide an election.";

        return getGSession().getNamedQuery("electionNominee.getConfirmedForElection")
                .setParameter("election", election)
                .setParameter("confirmedStatus", NomineeStatus.CONFIRMED)
                .setParameter("excludeUserOid", exists(excludeUser) ? excludeUser.getOid() : OID.DUMMY_OID)
                .setParameter("confirmedBefore", confirmedBefore != null ? confirmedBefore : Instant.now())
                .setMaxResults(count)
                .list();
    }
}
