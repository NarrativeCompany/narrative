package org.narrative.network.core.rating;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.network.core.user.User;
import org.narrative.shared.event.reputation.RatingType;

public interface RatingService {
    <R extends Ratable<R>,
            RV extends RatingValue<URO>,
            URO extends UserRatedObject<DAO,RV>,
            DAO extends UserRatedObjectDAO<R, URO, OID, RV>>
    URO setRating(R ratable, User user, RatingType ratingType, RV ratingValue, String reason);
}
