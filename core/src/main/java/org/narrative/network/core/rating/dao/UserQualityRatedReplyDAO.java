package org.narrative.network.core.rating.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

public class UserQualityRatedReplyDAO extends CompositionDAOImpl<UserQualityRatedReply, OID> implements UserRatedObjectDAO<Reply, UserQualityRatedReply, OID, QualityRating>{
    public UserQualityRatedReplyDAO() {
        super(UserQualityRatedReply.class);
    }

    public UserQualityRatedReply getForUserReply(User user, Reply reply) {
        return (UserQualityRatedReply) getGSession()
                .getNamedQuery("userQualityRatedReply.getForUserReply")
                .setParameter("userOid", user.getOid())
                .setParameter("reply", reply)
                .uniqueResult();
    }

    @Override
    public UserQualityRatedReply getRatingForUser(User user, Reply reply) {
        return getForUserReply(user, reply);
    }

    @Override
    public UserQualityRatedReply createRatingForUser(User user, QualityRating qualityRating, Reply reply) {
        return new UserQualityRatedReply(user, qualityRating, reply);
    }

    @Override
    public Map<OID, QualityRating> getRatingsForUsersAndValues(Reply reply, Set<QualityRating> qualityRatings) {
        List<ObjectPair<OID,QualityRating>> pairs = getGSession()
                .getNamedQuery("userQualityRatedReply.getRatingsForUsersAndValues")
                .setParameter("reply", reply)
                .setParameterList("qualityRatings", qualityRatings)
                .list();
        return ObjectPair.getAsMap(pairs);
    }

    public Map<OID, QualityRating> getReplyOidToQualityRatingByUser(User user, List<Reply> replies) {
        if (isEmptyOrNull(replies)) {
            return Collections.emptyMap();
        }

        List<ObjectPair<OID, QualityRating>> results = (List<ObjectPair<OID, QualityRating>>) getGSession()
                .getNamedQuery("userQualityRatedReply.getReplyOidToQualityRatingByUser")
                .setParameter("userOid", user.getOid())
                .setParameterList("replies", replies)
                .list();

        return ObjectPair.getAsMap(results);
    }

    public void populateQualityRatingsForReplies(User user, List<Reply> replies) {
        if (isEmptyOrNull(replies)) {
            return;
        }

        Map<OID, QualityRating> ratingLookup = exists(user)
            ? getReplyOidToQualityRatingByUser(user, replies)
            : Collections.emptyMap();

        for (Reply reply : replies) {
            reply.setQualityRatingByCurrentUser(ratingLookup.get(reply.getOid()));
        }
    }

    public int deleteAllForComposition(Composition composition) {
        return getGSession().getNamedQuery("userQualityRatedReply.deleteAllForComposition")
                .setParameter("compositionOid", composition.getOid())
                .executeUpdate();
    }

    public long getCountForUserAfter(User user, Instant after) {
        return getGSession().createNamedQuery("userQualityRatedReply.getCountForUserAfter", Number.class)
                .setParameter("userOid", user.getOid())
                .setParameter("after", after)
                .uniqueResult().longValue();
    }
}
