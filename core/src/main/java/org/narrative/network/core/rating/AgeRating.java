package org.narrative.network.core.rating;

import com.google.common.collect.Sets;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.rating.model.AgeRatingFields;
import org.narrative.network.core.rating.model.UserAgeRatedObject;
import org.narrative.shared.event.reputation.RatingType;

import java.util.Set;

/**
 * Date: 2019-01-03
 * Time: 11:49
 *
 * @author jonmark
 */
public enum AgeRating implements IntegerEnum, RatingValue<UserAgeRatedObject> {
    GENERAL(0, 0) {
        @Override
        public void addVotePoints(AgeRatingFields ratingFields, int votePoints) {
            ratingFields.setGeneralCount(ratingFields.getGeneralCount() + 1);
            ratingFields.setGeneralPoints(ratingFields.getGeneralPoints() + votePoints);
        }

        @Override
        public void removeVotePoints(AgeRatingFields ratingFields, int votePoints) {
            ratingFields.setGeneralCount(Math.max(0, ratingFields.getGeneralCount() - 1));
            ratingFields.setGeneralPoints(Math.max(0, ratingFields.getGeneralPoints() - votePoints));
        }
    }
    ,RESTRICTED(1, 18) {
        @Override
        public void addVotePoints(AgeRatingFields ratingFields, int votePoints) {
            ratingFields.setRestrictedCount(ratingFields.getRestrictedCount() + 1);
            ratingFields.setRestrictedPoints(ratingFields.getRestrictedPoints() + votePoints);
        }

        @Override
        public void removeVotePoints(AgeRatingFields ratingFields, int votePoints) {
            ratingFields.setRestrictedCount(Math.max(0, ratingFields.getRestrictedCount() - 1));
            ratingFields.setRestrictedPoints(Math.max(0, ratingFields.getRestrictedPoints() - votePoints));
        }
    }
    ;

    private final int id;
    private final int minimumAgeYears;

    public static final Set<AgeRating> ALL_AUDIENCES_ONLY = Sets.immutableEnumSet(GENERAL);
    public static final Set<AgeRating> ALL_AGE_RATINGS = Sets.immutableEnumSet(GENERAL, RESTRICTED);

    AgeRating(int id, int minimumAgeYears) {
        this.id = id;
        this.minimumAgeYears = minimumAgeYears;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public RatingType getRatingType() {
        return RatingType.AGE;
    }

    protected abstract void addVotePoints(AgeRatingFields ratingFields, int votePoints);
    protected abstract void removeVotePoints(AgeRatingFields ratingFields, int votePoints);

    @Override
    public <R extends Ratable<R>> void addVote(R ratable, UserAgeRatedObject userRatedObject, String reason) {
        addVotePoints(ratable.getRatingFields(getRatingType()), userRatedObject.getPointValue());
    }

    @Override
    public <R extends Ratable<R>> void removeVote(R ratable, UserAgeRatedObject userRatedObject) {
        removeVotePoints(ratable.getRatingFields(getRatingType()), userRatedObject.getPointValue());
    }

    public int getMinimumAgeYears() {
        return minimumAgeYears;
    }

    /**
     * Return a set of {@link AgeRating} that are permissible for the passed in age in years.
     * @param ageYears The age in years to test against. null if the age is unknown
     * @return A set of permitted ratings for the age passed in
     */
    public static Set<AgeRating> getPermittedRatingsForAge(Integer ageYears) {
        // For efficiency, return a pre-calculated EnumSet based on the passed in age
        if (ageYears!=null && ageYears >= RESTRICTED.minimumAgeYears) {
            return ALL_AGE_RATINGS;
        }
        return ALL_AUDIENCES_ONLY;
    }

    public boolean isRestricted() {
        return this == RESTRICTED;
    }

    public static boolean ageRatingsContainRestricted(Set<AgeRating> ageRatings) {
        return ageRatings.contains(RESTRICTED);
    }
}