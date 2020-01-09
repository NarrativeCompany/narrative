package org.narrative.network.core.rating.model;

import com.google.common.annotations.VisibleForTesting;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.RatingFields;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
@Embeddable
@Data
@NoArgsConstructor
@FieldNameConstants
public class QualityRatingFields implements RatingFields<QualityRating> {

    public static final int DEFAULT_SCORE = 50;
    /**
     * we'll show the score once 0.1 vote points have been attained
     */
    public static final int MINIMUM_POINTS_TO_SHOW_SCORE = UserReputation.MAX_POINTS_PER_VOTE/10;
    public static final int MINIMUM_POINTS_TO_SHOW_PROGRESS = 3*UserReputation.MAX_POINTS_PER_VOTE;

    private int likeCount;
    private int likePoints;

    private int dislikeContentViolatesAupCount;
    private int dislikeContentViolatesAupPoints;

    private int dislikeLowQualityContentCount;
    private int dislikeLowQualityContentPoints;

    private int dislikeDisagreeWithViewpointCount;
    private int dislikeDisagreeWithViewpointPoints;

    private int score;

    public QualityRatingFields(boolean init) {
        if(init) {
            // bl: all items default to a score of 50 until at least 1 vote point has been accumulated
            setScore(DEFAULT_SCORE);
        }
    }

    @Transient
    @Override
    public QualityRating getEffectiveRatingValue() {
        QualityRating rating = null;
        int ratingPoints = 0;
        // bl: the logic here doesn't deal with ties. first-come, first-served instead.
        // so, the order here matters for precedence purposes.
        if(getLikePoints() > ratingPoints) {
            rating = QualityRating.LIKE;
            ratingPoints = getLikePoints();
        }
        if(getDislikeContentViolatesAupPoints() > ratingPoints) {
            rating = QualityRating.DISLIKE_CONTENT_VIOLATES_AUP;
            ratingPoints = getDislikeContentViolatesAupPoints();
        }
        if(getDislikeLowQualityContentPoints() > ratingPoints) {
            rating = QualityRating.DISLIKE_LOW_QUALITY_CONTENT;
            ratingPoints = getDislikeLowQualityContentPoints();
        }
        // bl: even though disagree with viewpoint doesn't count toward the score,
        // it's still technically a value that can be in correlation from a reputation consensus standpoint.
        // thus, support it here purely for correlation purposes. it makes the correlation formula simpler
        // to not have to deal with an edge case where we could return null here when there are actually votes.
        if(getDislikeDisagreeWithViewpointPoints() > ratingPoints) {
            rating = QualityRating.DISLIKE_DISAGREE_WITH_VIEWPOINT;
            ratingPoints = getDislikeDisagreeWithViewpointPoints();
        }
        return rating;
    }

    @Transient
    @Override
    public int getTotalVoteCount() {
        return getLikeCount()+
                getDislikeContentViolatesAupCount()+
                getDislikeLowQualityContentCount()+
                getDislikeDisagreeWithViewpointCount();
    }

    @Transient
    @Override
    public int getTotalVotePoints() {
        // bl: we don't include dislikes for disagreeing with viewpoint in the total. that dislike is "empty"
        // in that it is recorded as a vote, but doesn't affect the effective rating or consensus.
        return getLikePoints()+
                getDislikeContentViolatesAupPoints()+
                getDislikeLowQualityContentPoints();
    }

    @Transient
    private boolean isScorePending() {
        return getTotalVotePoints() < MINIMUM_POINTS_TO_SHOW_PROGRESS;
    }

    @Transient
    @VisibleForTesting
    public int getCalculatedScore() {
        // bl: return the default score until we have enough vote points to show progress.
        // this is necessary so that the value we store in the database is fixed at 50 / "Medium"
        // until we get at least 3 vote points
        if (isScorePending()) {
            return DEFAULT_SCORE;
        }

        return getRawScore();
    }

    @Transient
    private int getRawScore() {
        return RatingFields.calculateScore(getLikePoints(), getTotalVotePoints());
    }

    public void recalculateScore() {
        setScore(getCalculatedScore());
    }

    @Transient
    public QualityLevel getQualityLevel() {
        // bl: if we haven't reached 3 vote points yet, then we're going to treat is as effectively medium since we don't have
        // enough evidence to cast judgment yet. this shouldn't be used on the front end. it should only be used
        // on the back end for quality level evaluation purposes.
        if (isScorePending()) {
            return QualityLevel.MEDIUM;
        }
        return QualityLevel.getQualityLevelForScore(getScore());
    }

    @Transient
    public QualityLevel getQualityLevelForApi() {
        // bl: if we haven't reached 3 vote points yet, then we don't know the effective quality level yet, so
        // we won't be returning the qualityLevel to the front end. this way, we can return a score earlier without a
        // qualityLevel to indicate that the score is "pending" further votes.
        if (isScorePending()) {
            return null;
        }
        return getQualityLevel();
    }

    @Transient
    @Override
    public Integer getScoreForApi() {
        Integer score = RatingFields.super.getScoreForApi();
        // bl: if we have a score, we have to make sure we use the proper score value
        // bl: if we haven't attained an official quality level yet (i.e. it's pending), then we need to calculate the
        // raw score; the score placed on the QualityRatingFields should be 50 (the default) in this case.
        if(score!=null && isScorePending()) {
            return getRawScore();
        }

        return score;
    }

    @Transient
    @Override
    public int getMinimumScoreForApi() {
        return MINIMUM_POINTS_TO_SHOW_SCORE;
    }
}
