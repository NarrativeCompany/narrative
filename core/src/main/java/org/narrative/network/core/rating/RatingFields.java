package org.narrative.network.core.rating;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface RatingFields<T extends RatingValue> {
    T getEffectiveRatingValue();
    int getTotalVoteCount();
    int getTotalVotePoints();
    int getScore();

    int getMinimumScoreForApi();

    default Integer getScoreForApi() {
        // jw: for now quality and age share the same logic here, so let's just leave well enough alone.
        if (getTotalVotePoints() < getMinimumScoreForApi()) {
            return null;
        }

        // jw: once there is enough headway, let's return a proper value.
        return getScore();
    }

    static int calculateScore(int rawPointsFor, int totalRawPoints) {
        BigDecimal likePoints = BigDecimal.valueOf(rawPointsFor);
        BigDecimal totalPoints = BigDecimal.valueOf(totalRawPoints);

        // jw: let's short out if there are no totalPoints, since it will result in a divide by zero error if we let it pass through.
        if (totalPoints.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        // bl: the quality score is a percentile on the scale of 0-100, so multiply by 100
        BigDecimal score = likePoints
                .multiply(BigDecimal.valueOf(100))
                .divide(totalPoints, 0, RoundingMode.HALF_UP);

        return score.intValueExact();
    }
}
