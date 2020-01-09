package org.narrative.network.core.rating.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.rating.model.UserAgeRatedComposition;
import org.narrative.network.core.user.User;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserAgeRatedCompositionDAO extends CompositionDAOImpl<UserAgeRatedComposition, OID> implements UserRatedObjectDAO<Content, UserAgeRatedComposition, OID, AgeRating> {
    public UserAgeRatedCompositionDAO() {
        super(UserAgeRatedComposition.class);
    }

    public UserAgeRatedComposition getForUserComposition(User user, Composition composition) {
        return (UserAgeRatedComposition) getGSession()
                .getNamedQuery("userAgeRatedComposition.getForUserComposition")
                .setParameter("userOid", user.getOid())
                .setParameter("composition", composition)
                .uniqueResult();
    }

    @Override
    public UserAgeRatedComposition getRatingForUser(User user, Content content) {
        return getForUserComposition(user, content.getComposition());
    }

    @Override
    public UserAgeRatedComposition createRatingForUser(User user, AgeRating ageRating, Content content) {
        return new UserAgeRatedComposition(user, ageRating, content.getComposition());
    }

    @Override
    public Map<OID, AgeRating> getRatingsForUsersAndValues(Content content, Set<AgeRating> ageRatings) {
        List<ObjectPair<OID,AgeRating>> pairs = getGSession()
                .getNamedQuery("userAgeRatedComposition.getRatingsForUsersAndValues")
                .setParameter("composition", content.getComposition())
                .setParameterList("ageRatings", ageRatings)
                .list();
        return ObjectPair.getAsMap(pairs);
    }

    public long getCountForUserAfter(User user, Instant after) {
        return getGSession().createNamedQuery("userAgeRatedComposition.getCountForUserAfter", Number.class)
                .setParameter("userOid", user.getOid())
                .setParameter("after", after)
                .uniqueResult().longValue();
    }
}
