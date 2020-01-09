package org.narrative.network.customizations.narrative.niches.referendum.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.ArrayList;
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
public class ReferendumDAO extends GlobalDAOImpl<Referendum, OID> {
    public ReferendumDAO() {
        super(Referendum.class);
    }

    public List<OID> getExpiredReferendumOids() {
        return getGSession().getNamedQuery("referendum.getExpiredReferendumOids").list();
    }

    public List<Referendum> getOpenReferendumsForNiche(Niche niche) {
        return getGSession().getNamedQuery("referendum.getOpenReferendumsForNiche").setParameter("niche", niche).list();
    }

    public List<Referendum> getOpenReferendumsOfTypesForNiche(Niche niche, Collection<ReferendumType> types) {
        return getGSession().getNamedQuery("referendum.getOpenReferendumsOfTypesForNiche").setParameterList("referendumTypes", types).setParameter("niche", niche).list();
    }

    public List<Referendum> getReferendumsByTypeAndStatus(Collection<ReferendumType> types, boolean open, int page, int resultsPerPage) {
        if(open) {
            return getOpenReferendumsByType(types, page, resultsPerPage);
        }
        return getCompletedReferendumsByType(types, page, resultsPerPage);
    }

    private List<Referendum> getOpenReferendumsByType(Collection<ReferendumType> types, int page, int resultsPerPage) {
        return getGSession().getNamedQuery("referendum.getOpenReferendumsByType")
                .setParameterList("referendumTypes", types)
                .setFirstResult(Math.max(0, page - 1) * resultsPerPage)
                .setMaxResults(resultsPerPage)
                .list();
    }

    private List<Referendum> getCompletedReferendumsByType(Collection<ReferendumType> types, int page, int resultsPerPage) {
        return getGSession().getNamedQuery("referendum.getCompletedReferendumsByType")
                .setParameterList("referendumTypes", types)
                .setFirstResult(Math.max(0, page - 1) * resultsPerPage)
                .setMaxResults(resultsPerPage)
                .list();
    }

    public long getCountOfReferendumsByTypeAndStatus(Collection<ReferendumType> referendumTypes, boolean open) {
        return ((Number)getGSession().getNamedQuery("referendum.getCountOfReferendumsByTypeAndStatus")
                .setParameter("open", open)
                .setParameterList("referendumTypes", referendumTypes)
                .uniqueResult())
                .longValue();
    }

    public List<ChannelConsumer> getChannelConsumersFromReferendums(List<Referendum> referendums) {
        if (isEmptyOrNull(referendums)) {
            return Collections.emptyList();
        }

        List<ChannelConsumer> channelConsumers = new ArrayList<>(referendums.size());
        for (Referendum referendum : referendums) {
            channelConsumers.add(referendum.getChannelConsumer());
        }

        return channelConsumers;
    }

    public void populateReferendumVotesByCurrentUser(AreaUserRlm areaUserRlm, Collection<Referendum> referendums) {
        // jw: if there are no niches, then there is nothing to do.
        if (isEmptyOrNull(referendums)) {
            return;
        }

        Map<Referendum,ReferendumVote> currentUserVotes;
        if(exists(areaUserRlm)) {
            currentUserVotes = ReferendumVote.dao().getByReferendumForVoter(referendums, areaUserRlm);
        } else {
            // if the current role is a guest, then we'll set everything to null for performance
            currentUserVotes = Collections.emptyMap();
        }

        // bl: make sure we set it for every Referendum, not just the ones with votes! that way, we won't do
        // JIT initialization of the Referendum.currentUserVote
        for (Referendum referendum : referendums) {
            ReferendumVote vote = currentUserVotes.get(referendum);
            referendum.setCurrentUserVote(vote);
        }
    }


    public List<Referendum> getAllForPublication(Publication publication) {
        return getAllBy(new NameValuePair<>(Referendum.FIELD__PUBLICATION__NAME, publication));
    }
}