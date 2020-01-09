package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.model.entity.ContentQualityEntity;
import org.narrative.reputation.repository.ContentQualityRepository;
import org.narrative.reputation.service.ContentQualityCalculatorService;
import org.narrative.shared.event.reputation.CommentLikeEvent;
import org.narrative.shared.event.reputation.ContentLikeEvent;
import org.narrative.shared.event.reputation.LikeEvent;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class ContentQualityCalculatorServiceImpl implements ContentQualityCalculatorService {
    private final ContentQualityRepository contentQualityRepository;

    @Autowired
    public ContentQualityCalculatorServiceImpl(ContentQualityRepository contentQualityRepository) {this.contentQualityRepository = contentQualityRepository;}

    @Override
    public LikeEvent updateContentQualityWithEvent(LikeEvent likeEvent) {
        log.info("updateContentQualityWithEvent with: {} " , likeEvent);

        // Query the current ContentQualityEntity for the current user
        Optional<ContentQualityEntity> optionalContentQualityEntity = contentQualityRepository.findById(likeEvent.getUserOid());

        // Create a new entity if one didn't exist
        ContentQualityEntity qualityEntity = optionalContentQualityEntity.orElseGet(() ->
                ContentQualityEntity.builder()
                        .userOid(likeEvent.getUserOid())
                        .build());

        // Set metadata
        qualityEntity.setLastEventId(likeEvent.getEventId());
        qualityEntity.setLastEventTimestamp(likeEvent.getEventTimestamp());

        // Increment contentRatingsReceivedCount
        qualityEntity.setContentRatingsReceivedCount(qualityEntity.getContentRatingsReceivedCount() + 1);

        // Calculate the new values based on event type
        double likePoints = likeEvent.getLikePoints();

        // process event based on type
        if (likeEvent instanceof ContentLikeEvent) {
            processContentLikeEvent(likeEvent, qualityEntity, likePoints);
        } else if (likeEvent instanceof CommentLikeEvent) {
            processCommentLikeEvent(likeEvent, qualityEntity, likePoints);
        }

        contentQualityRepository.save(qualityEntity);

        return likeEvent;
    }

    @Override
    @Transactional(readOnly = true)
    public double getContentQualityScoreForUser(long userOid) {
        log.info("getContentQualityScoreForUser with: {} " , userOid);

        // Query the current ContentQualityEntity for the current user or null if the user is not found
        ContentQualityEntity contentQualityEntity = contentQualityRepository.findById(userOid).orElse(null);

        if (contentQualityEntity == null){
            return 0;
        }

        // Calculate grossQualityValue
        double divisor = ((contentQualityEntity.getContentLikePoints() + contentQualityEntity.getContentDislikePoints()) * 3 + contentQualityEntity.getCommentLikePoints() + contentQualityEntity.getCommentDislikePoints());
        double grossQualityValue = 0.0;
        // Prevent divide by zero
        if (divisor != 0) {
            grossQualityValue = (contentQualityEntity.getContentLikePoints() * 3 + contentQualityEntity.getCommentLikePoints()) / divisor;
        }

        // Get valueMultiplier
        double valueMultiplier = getQualityValueMultiplier( contentQualityEntity.getContentRatingsReceivedCount());

        // Return net quality value
        return grossQualityValue * valueMultiplier * 100;
    }

    public static double getQualityValueMultiplier(int itemCount) {
        double valueMultiplier = 1.00;
        if (itemCount < 5) {
            valueMultiplier = 0;
        } else if (itemCount < 25) {
            valueMultiplier = 0.25;
        } else if (itemCount < 50) {
            valueMultiplier= 0.5;
        } else if (itemCount < 100) {
            valueMultiplier = 0.75;
        }
        return valueMultiplier;
    }

    private void processContentLikeEvent(LikeEvent likeEvent, ContentQualityEntity contentQualityEntity, double likePoints) {
        if (likeEvent.getLikeEventType().isLike()) {
            contentQualityEntity.setContentLikePoints(contentQualityEntity.getContentLikePoints() + likePoints);
        } else if (likeEvent.getLikeEventType().isDislike()) {
            contentQualityEntity.setContentDislikePoints(contentQualityEntity.getContentDislikePoints() + likePoints);
        }
    }

    private void processCommentLikeEvent(LikeEvent likeEvent, ContentQualityEntity contentQualityEntity, double likePoints) {
        if (likeEvent.getLikeEventType().isLike()) {
            contentQualityEntity.setCommentLikePoints(contentQualityEntity.getCommentLikePoints() + likePoints);
        } else if (likeEvent.getLikeEventType().isDislike()) {
            contentQualityEntity.setCommentDislikePoints(contentQualityEntity.getCommentDislikePoints() + likePoints);
        }
    }
}
