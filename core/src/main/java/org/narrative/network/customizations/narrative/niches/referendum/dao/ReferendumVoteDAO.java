package org.narrative.network.customizations.narrative.niches.referendum.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class ReferendumVoteDAO extends GlobalDAOImpl<ReferendumVote, OID> {
    public ReferendumVoteDAO() {
        super(ReferendumVote.class);
    }

    public int getCount(Referendum referendum) {
        return ((Number) getGSession().getNamedQuery("referendumVote.getCount")
                .setParameter("referendum", referendum)
                .uniqueResult())
                .intValue();
    }

    public ReferendumVote getForReferendumAndVoter(Referendum referendum, AreaUserRlm voter) {
        return (ReferendumVote) getGSession().getNamedQuery("referendumVote.getForReferendumAndVoter").setParameter("referendum", referendum).setParameter("voter", voter).uniqueResult();
    }

    public Map<Referendum, ReferendumVote> getByReferendumForVoter(Collection<Referendum> referendums, AreaUserRlm voter) {
        if (isEmptyOrNull(referendums)) {
            return Collections.emptyMap();
        }

        Collection<ObjectPair<Referendum, ReferendumVote>> data = getGSession().getNamedQuery("referendumVote.getByReferendumForVoter").setParameterList("referendums", referendums).setParameter("voter", voter).list();

        return ObjectPair.getAsMap(data);
    }

    public List<ReferendumVote> getRecentVotes(Referendum referendum, Boolean votedFor, String lastDisplayName, String lastUsername, int results) {
        return getGSession().getNamedQuery("referendumVote.getRecentVotes")
                .setParameter("referendum", referendum)
                .setParameter("votedFor", votedFor)
                .setParameter("lastDisplayName", isEmpty(lastDisplayName) ? null : lastDisplayName)
                .setParameter("lastUsername", isEmpty(lastUsername) ? null : lastUsername)
                .setMaxResults(results)
                .list();
    }

    public Collection<OID> getVoterAreaUserOids(Referendum referendum) {
        return getGSession().getNamedQuery("referendumVote.getVoterAreaUserOids").setParameter("referendum", referendum).list();
    }

    public Map<OID, Boolean> getUserVotesForReferendum(Referendum referendum) {
        List<ObjectPair<OID, Boolean>> results = getGSession().getNamedQuery("referendumVote.getUserVotesForReferendum")
                .setParameter("referendum", referendum)
                .setMaxResults(Integer.MAX_VALUE)
                .list();

        return ObjectPair.getAsMap(results);
    }

    public ReferendumVote getForReferendumAndComment(Referendum referendum, Reply reply) {
        return (ReferendumVote)getGSession().getNamedQuery("referendumVote.getForReferendumAndComment")
                .setParameter("referendum", referendum)
                .setParameter("commentReplyOid", reply.getOid())
                .uniqueResult();
    }
}