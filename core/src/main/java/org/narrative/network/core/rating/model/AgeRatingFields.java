package org.narrative.network.core.rating.model;

import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.RatingFields;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
@Embeddable
@Data
public class AgeRatingFields implements RatingFields<AgeRating> {
    private int generalCount;
    private int generalPoints;

    private int restrictedCount;
    private int restrictedPoints;

    @Transient
    @Override
    public AgeRating getEffectiveRatingValue() {
        // bl: we must have vote points in order to have a RatingValue
        if(getTotalVotePoints()<=0) {
            return null;
        }
        return getGeneralPoints()>=getRestrictedPoints() ? AgeRating.GENERAL : AgeRating.RESTRICTED;
    }

    @Transient
    @Override
    public int getTotalVoteCount() {
        return getGeneralCount()+getRestrictedCount();
    }

    @Transient
    @Override
    public int getTotalVotePoints() {
        return getGeneralPoints()+getRestrictedPoints();
    }

    @Transient
    @Override
    public int getScore() {
        return RatingFields.calculateScore(getGeneralPoints(), getTotalVotePoints());
    }

    @Transient
    @Override
    public int getMinimumScoreForApi() {
        // bl: the community age rating will trump the author's age rating once more than 1 vote point has been attained
        return UserReputation.MIN_POINTS_FOR_TWO_VOTES;
    }
}
