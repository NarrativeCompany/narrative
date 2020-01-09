package org.narrative.reputation.service.impl

import org.narrative.reputation.model.entity.ContentQualityEntity
import org.narrative.reputation.repository.ContentQualityRepository
import org.narrative.reputation.service.ContentQualityCalculatorService
import org.narrative.shared.event.reputation.CommentLikeEvent
import org.narrative.shared.event.reputation.ContentLikeEvent
import org.narrative.shared.event.reputation.LikeEvent
import org.narrative.shared.event.reputation.LikeEventType
import spock.lang.Specification
import spock.lang.Unroll

class ContentQualityCalculatorServiceImplSpec extends Specification {
    def "test updateContentQualityWithEvent for new user"() {
        given:
            ContentQualityRepository contentQualityRepository = Mock()
            ContentQualityCalculatorService contentQualityCalculatorService = new ContentQualityCalculatorServiceImpl(contentQualityRepository)

            contentQualityRepository.findById(_) >> Optional.empty()

            LikeEvent likeEvent = event

        when:
            contentQualityCalculatorService.updateContentQualityWithEvent(likeEvent)
        then:
            1 * contentQualityRepository.save(*_) >> { ContentQualityEntity contentQualityEntity ->
                assert contentQualityEntity.userOid == 1
                assert contentQualityEntity.contentLikePoints == contentLikePoints
                assert contentQualityEntity.contentDislikePoints == contentDislikePoints
                assert contentQualityEntity.commentLikePoints == commentLikePoints
                assert contentQualityEntity.commentDislikePoints == commentDislikePoints
            }
        where:
            event            || contentLikePoints | contentDislikePoints | commentLikePoints | commentDislikePoints
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.LIKE)
                    .likePoints(1)
                    .build() || 1                 | 0                    | 0                 | 0
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE)
                    .likePoints(1)
                    .build() || 0                 | 1                    | 0                 | 0
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE_VIEWPOINT)
                    .likePoints(1)
                    .build() || 0                 | 0                    | 0                 | 0
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.LIKE)
                    .likePoints(1)
                    .build() || 0                 | 0                    | 1                 | 0
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE)
                    .likePoints(1)
                    .build() || 0                 | 0                    | 0                 | 1
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE_VIEWPOINT)
                    .likePoints(1)
                    .build() || 0                 | 0                    | 0                 | 0

    }

    def "test updateContentQualityWithEvent for existing user"() {
        given:
            ContentQualityRepository contentQualityRepository = Mock()
            ContentQualityCalculatorService contentQualityCalculatorService = new ContentQualityCalculatorServiceImpl(contentQualityRepository)

            contentQualityRepository.findById(_) >> Optional.of(ContentQualityEntity.builder()
                    .userOid(1)
                    .contentLikePoints(20)
                    .contentDislikePoints(30)
                    .commentLikePoints(40)
                    .commentDislikePoints(50)
                    .build()
            )

            LikeEvent likeEvent = event

        when:
            contentQualityCalculatorService.updateContentQualityWithEvent(likeEvent)
        then:
            1 * contentQualityRepository.save(*_) >> { ContentQualityEntity contentQualityEntity ->
                assert contentQualityEntity.userOid == 1
                assert contentQualityEntity.contentLikePoints == contentLikePoints
                assert contentQualityEntity.contentDislikePoints == contentDislikePoints
                assert contentQualityEntity.commentLikePoints == commentLikePoints
                assert contentQualityEntity.commentDislikePoints == commentDislikePoints
            }
        where:
            event            || contentLikePoints | contentDislikePoints | commentLikePoints | commentDislikePoints
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.LIKE)
                    .likePoints(1)
                    .build() || 21                | 30                   | 40                | 50
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE)
                    .likePoints(1)
                    .build() || 20                | 31                   | 40                | 50
            ContentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE_VIEWPOINT)
                    .likePoints(1)
                    .build() || 20                | 30                   | 40                | 50
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.LIKE)
                    .likePoints(1)
                    .build() || 20                | 30                   | 41                | 50
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE)
                    .likePoints(1)
                    .build() || 20                | 30                   | 40                | 51
            CommentLikeEvent.builder()
                    .userOid(1)
                    .likeEventType(LikeEventType.DISLIKE_VIEWPOINT)
                    .likePoints(1)
                    .build() || 20                | 30                   | 40                | 50

    }

    @Unroll
    def "getContentQualityScoreForUser test"() {
        given:
            ContentQualityRepository contentQualityRepository = Mock()
            ContentQualityCalculatorService contentQualityCalculatorService = new ContentQualityCalculatorServiceImpl(contentQualityRepository)

            contentQualityRepository.findById(_) >> Optional.of(ContentQualityEntity.builder()
                    .userOid(1)
                    .contentLikePoints(contentLikePoints)
                    .contentDislikePoints(contentDislikePoints)
                    .commentLikePoints(commentLikePoints)
                    .commentDislikePoints(commentDislikePoints)
                    .contentRatingsReceivedCount(contentRatingsReceivedCount)
                    .build()
            )

        when:
            def score = contentQualityCalculatorService.getContentQualityScoreForUser(1)
        then:
            score == contentQualityScore
        where:
            contentLikePoints | contentDislikePoints | commentLikePoints | commentDislikePoints | contentRatingsReceivedCount || contentQualityScore
            1                 | 0                    | 0                 | 0                    | 1                           || 0
            6                 | 0                    | 4                 | 0                    | 24                          || 25
            6                 | 0                    | 4                 | 0                    | 25                          || 50
            52                | 0                    | 48                | 0                    | 49                          || 50
            52                | 0                    | 48                | 0                    | 50                          || 75
            52                | 0                    | 48                | 0                    | 99                          || 75
            52                | 0                    | 48                | 0                    | 100                         || 100
            100               | 0                    | 0                 | 0                    | 400                         || 100
            100               | 0                    | 0                 | 0                    | 1000                        || 100
            50                | 50                   | 50                | 50                   | 1000                        || 50
            50                | 0                    | 50                | 50                   | 1000                        || 80
            732               | 4312                 | 36                | 0                    | 81                          || 11.036392405063292
            0                 | 0                    | 12541             | 0                    | 276                         || 100
            0                 | 0                    | 22799             | 69                   | 473                         || 99.6982683225468
            2176              | 0                    | 75                | 0                    | 43                          || 50
    }

}
