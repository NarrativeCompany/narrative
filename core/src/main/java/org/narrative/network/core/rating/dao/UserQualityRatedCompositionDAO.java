package org.narrative.network.core.rating.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.model.UserQualityRatedComposition;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserQualityRatedCompositionDAO extends CompositionDAOImpl<UserQualityRatedComposition, OID> implements UserRatedObjectDAO<Content, UserQualityRatedComposition, OID, QualityRating> {
    public UserQualityRatedCompositionDAO() {
        super(UserQualityRatedComposition.class);
    }

    public UserQualityRatedComposition getForUserComposition(User user, Composition composition) {
        return (UserQualityRatedComposition) getGSession()
                .getNamedQuery("userQualityRatedComposition.getForUserComposition")
                .setParameter("userOid", user.getOid())
                .setParameter("composition", composition)
                .uniqueResult();
    }

    @Override
    public UserQualityRatedComposition getRatingForUser(User user, Content content) {
        return getForUserComposition(user, content.getComposition());
    }

    @Override
    public UserQualityRatedComposition createRatingForUser(User user, QualityRating qualityRating, Content content) {
        return new UserQualityRatedComposition(user, qualityRating, content.getComposition());
    }

    @Override
    public Map<OID, QualityRating> getRatingsForUsersAndValues(Content content, Set<QualityRating> qualityRatings) {
        List<ObjectPair<OID,QualityRating>> pairs = getGSession()
                .getNamedQuery("userQualityRatedComposition.getRatingsForUsersAndValues")
                .setParameter("composition", content.getComposition())
                .setParameterList("qualityRatings", qualityRatings)
                .list();
        return ObjectPair.getAsMap(pairs);
    }

    public long getCountForUserAfter(User user, Instant after) {
        return getGSession().createNamedQuery("userQualityRatedComposition.getCountForUserAfter", Number.class)
                .setParameter("userOid", user.getOid())
                .setParameter("after", after)
                .uniqueResult().longValue();
    }
}
