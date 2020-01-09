package org.narrative.network.core.rating;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.area.base.services.ItemHourTrendingStatsManager;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.core.rating.model.UserQualityRatedObject;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.reputation.services.CreateEventMessageTask;
import org.narrative.network.customizations.narrative.service.impl.tribunal.SendAupReportToNarrativeStaffEmailTask;
import org.narrative.shared.event.reputation.CommentLikeEvent;
import org.narrative.shared.event.reputation.ContentLikeEvent;
import org.narrative.shared.event.reputation.LikeEvent;
import org.narrative.shared.event.reputation.LikeEventType;
import org.narrative.shared.event.reputation.RatingType;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public enum QualityRating implements IntegerEnum, RatingValue<UserQualityRatedObject> {
    LIKE(0) {
        @Override
        public void addVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setLikeCount(ratingFields.getLikeCount() + 1);
            ratingFields.setLikePoints(ratingFields.getLikePoints() + votePoints);
        }

        @Override
        public void removeVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setLikeCount(Math.max(0, ratingFields.getLikeCount() - 1));
            ratingFields.setLikePoints(Math.max(0, ratingFields.getLikePoints() - votePoints));
        }

        @Override
        protected LikeEventType getLikeEventType() {
            return LikeEventType.LIKE;
        }
    },
    DISLIKE_CONTENT_VIOLATES_AUP(1) {
        @Override
        public void addVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeContentViolatesAupCount(ratingFields.getDislikeContentViolatesAupCount() + 1);
            ratingFields.setDislikeContentViolatesAupPoints(ratingFields.getDislikeContentViolatesAupPoints() + votePoints);
        }

        @Override
        public void removeVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeContentViolatesAupCount(Math.max(0, ratingFields.getDislikeContentViolatesAupCount() - 1));
            ratingFields.setDislikeContentViolatesAupPoints(Math.max(0, ratingFields.getDislikeContentViolatesAupPoints() - votePoints));
        }

        @Override
        protected LikeEventType getLikeEventType() {
            return LikeEventType.DISLIKE;
        }
    },
    DISLIKE_LOW_QUALITY_CONTENT(2) {
        @Override
        public void addVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeLowQualityContentCount(ratingFields.getDislikeLowQualityContentCount() + 1);
            ratingFields.setDislikeLowQualityContentPoints(ratingFields.getDislikeLowQualityContentPoints() + votePoints);
        }

        @Override
        public void removeVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeLowQualityContentCount(Math.max(0, ratingFields.getDislikeLowQualityContentCount() - 1));
            ratingFields.setDislikeLowQualityContentPoints(Math.max(0, ratingFields.getDislikeLowQualityContentPoints() - votePoints));
        }

        @Override
        protected LikeEventType getLikeEventType() {
            return LikeEventType.DISLIKE;
        }
    },
    DISLIKE_DISAGREE_WITH_VIEWPOINT(3) {
        @Override
        public void addVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeDisagreeWithViewpointCount(ratingFields.getDislikeDisagreeWithViewpointCount() + 1);
            ratingFields.setDislikeDisagreeWithViewpointPoints(ratingFields.getDislikeDisagreeWithViewpointPoints() + votePoints);
        }

        @Override
        public void removeVotePoints(QualityRatingFields ratingFields, int votePoints) {
            ratingFields.setDislikeDisagreeWithViewpointCount(Math.max(0, ratingFields.getDislikeDisagreeWithViewpointCount() - 1));
            ratingFields.setDislikeDisagreeWithViewpointPoints(Math.max(0, ratingFields.getDislikeDisagreeWithViewpointPoints() - votePoints));
        }

        @Override
        protected LikeEventType getLikeEventType() {
            return LikeEventType.DISLIKE_VIEWPOINT;
        }
    }
    ;

    private final int id;

    QualityRating(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public RatingType getRatingType() {
        return RatingType.QUALITY;
    }

    protected abstract void addVotePoints(QualityRatingFields ratingFields, int votePoints);
    protected abstract void removeVotePoints(QualityRatingFields ratingFields, int votePoints);
    protected abstract LikeEventType getLikeEventType();

    @Override
    public final <R extends Ratable<R>> void addVote(R ratable, UserQualityRatedObject userRatedObject, String reason) {
        int pointValue = userRatedObject.getPointValue();
        addVotePoints(ratable.getRatingFields(getRatingType()), pointValue);
        // bl: if this is content, then add the points to the trending formula. we used to use the absolute pointValue here,
        // but after further consideration, it really doesn't make sense for content to trend when people are downvoting it.
        // further, your reward amount will go up if you get a lot of downvotes, which is counter-intuitive.
        if(ratable.getRatableType().isContent() && isCountPoints()) {
            ItemHourTrendingStatsManager.addContentLikePoints(userRatedObject.getUser(), cast(ratable, Content.class), isLike() ? pointValue : -pointValue);
        }
        // bl: send AUP violation notifications to the Tribunal
        QualityRating qualityRating = userRatedObject.getRatingValue();
        if(qualityRating.isDislikeContentViolatesAup()) {
            areaContext().doAreaTask(new SendAupReportToNarrativeStaffEmailTask(userRatedObject.getUser(), ratable, reason));
        }
        User ratableAuthor = ratable.getUser();
        if(exists(ratableAuthor)) {
            sendLikeEvent(ratable.getRatableType(), ratableAuthor.getOid(), pointValue);
        }
    }

    @Override
    public final <R extends Ratable<R>> void removeVote(R ratable, UserQualityRatedObject userRatedObject) {
        int pointValue = userRatedObject.getPointValue();
        removeVotePoints(ratable.getRatingFields(getRatingType()), pointValue);
        // bl: if this is content, then add the points to the trending formula. note that pointValues are _absolute_ values for trending purposes.
        // this means that a dislike counts equally as a like (reputation-adjusted) for trending purposes. this way,
        // something with a lot of likes and dislikes will trend. after all, the simple act of liking/disliking is
        // evidence of trending. note that we will exclude low quality content from trending, so if it's purely
        // receiving downvotes, then it won't artificially trend
        if(ratable.getRatableType().isContent()) {
            ItemHourTrendingStatsManager.removeContentLikePoints(userRatedObject.getUser(), cast(ratable, Content.class), userRatedObject.getRatingDatetime().toEpochMilli(), pointValue);
        }
        User ratableAuthor = ratable.getUser();
        if(exists(ratableAuthor)) {
            // bl: for removing a vote, send a like event with negative point values so they are removed
            sendLikeEvent(ratable.getRatableType(), ratableAuthor.getOid(), -pointValue);
        }
    }

    private void sendLikeEvent(RatableType ratableType, OID userOid, int pointValue) {
        // bl: send a LikeEvent with positive point values
        LikeEvent likeEvent = getLikeEvent(ratableType, userOid, pointValue);
        networkContext().doGlobalTask(new CreateEventMessageTask(likeEvent));
    }

    private LikeEvent getLikeEvent(RatableType ratableType, OID userOid, int pointValue) {
        if(ratableType.isContent()) {
            return ContentLikeEvent.builder()
                    .likeEventType(getLikeEventType())
                    .likePoints(pointValue)
                    .userOid(userOid.getValue())
                    .build();
        }

        assert ratableType.isReply() : "Found unsupported RatableType/" + ratableType;
        return CommentLikeEvent.builder()
                .likeEventType(getLikeEventType())
                .likePoints(pointValue)
                .userOid(userOid.getValue())
                .build();
    }

    public boolean isCountPoints() {
        // bl: all like points count except for disliking for disagreeing with viewpoint
        return !isDislikeDisagreeWithViewpoint();
    }

    public boolean isLike() {
        return this==LIKE;
    }

    public boolean isDislikeContentViolatesAup() {
        return this==DISLIKE_CONTENT_VIOLATES_AUP;
    }

    public boolean isDislikeDisagreeWithViewpoint() {
        return this==DISLIKE_DISAGREE_WITH_VIEWPOINT;
    }
}
