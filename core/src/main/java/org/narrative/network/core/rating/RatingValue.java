package org.narrative.network.core.rating;

import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.shared.event.reputation.RatingType;

public interface RatingValue<T extends UserRatedObject> {

    RatingType getRatingType();

    <R extends Ratable<R>> void addVote(R ratable, T userRatedObject, String reason);

    <R extends Ratable<R>> void removeVote(R ratable, T userRatedObject);
}
