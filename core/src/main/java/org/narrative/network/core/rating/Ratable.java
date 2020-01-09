package org.narrative.network.core.rating;

import org.narrative.network.core.user.User;
import org.narrative.shared.event.reputation.RatingType;

import java.sql.Timestamp;

public interface Ratable<T extends Ratable<T>> {
    RatableType getRatableType();

    <RF extends RatingFields> RF getRatingFields(RatingType ratingType);

    void onRatingUpdate(RatingType ratingType);

    Integer getRatingMultiplier(User user);

    void refreshForLock();

    Timestamp getLiveDatetime();

    User getUser();

    String getExtractForEmail();
}
