package org.narrative.network.core.rating.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.Ratable;
import org.narrative.network.core.rating.RatingValue;
import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAO;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface UserRatedObjectDAO<R extends Ratable<R>, T extends UserRatedObject, ID extends Serializable, RV extends RatingValue> extends NetworkDAO<T,ID> {
    T getRatingForUser(User user, R ratable);
    T createRatingForUser(User user, RV ratingValue, R ratable);
    Map<OID,RV> getRatingsForUsersAndValues(R ratable, Set<RV> ratingValues);
}
