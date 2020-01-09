package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.CurrentReputationEntity;
import org.narrative.reputation.model.entity.FollowerQualityEntity;
import org.narrative.reputation.repository.CurrentReputationRepository;
import org.narrative.reputation.repository.FollowerQualityRepository;
import org.narrative.reputation.service.ContentQualityCalculatorService;
import org.narrative.reputation.service.RatingCorrelationService;
import org.narrative.reputation.service.TotalReputationScoreCalculatorService;
import org.narrative.reputation.service.VoteCorrelationService;
import org.narrative.shared.calculator.TotalReputationCalculator;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

import static org.narrative.shared.reputation.config.ReputationConstants.*;

@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class TotalReputationScoreCalculatorServiceImpl implements TotalReputationScoreCalculatorService {

    private final CurrentReputationRepository currentReputationRepository;
    private final ContentQualityCalculatorService contentQualityCalculatorService;
    private final VoteCorrelationService voteCorrelationService;
    private final RatingCorrelationService ratingCorrelationService;
    private final FollowerQualityRepository followerQualityRepository;

    @Autowired
    public TotalReputationScoreCalculatorServiceImpl(ReputationProperties reputationProperties,
                                                     CurrentReputationRepository currentReputationRepository,
                                                     ContentQualityCalculatorService contentQualityCalculatorService,
                                                     VoteCorrelationService voteCorrelationService,
                                                     RatingCorrelationService ratingCorrelationService,
                                                     FollowerQualityRepository followerQualityRepository
    ) {
        this.currentReputationRepository = currentReputationRepository;
        this.contentQualityCalculatorService = contentQualityCalculatorService;
        this.voteCorrelationService = voteCorrelationService;
        this.ratingCorrelationService = ratingCorrelationService;
        this.followerQualityRepository = followerQualityRepository;
    }

    @Override
    public void calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(Set<Long> userIdSet) {
        log.info("calculateTotalScoreAndUpdateCurrentReputationEntityForUsers with: {} ", userIdSet);

        userIdSet.forEach((userOid) -> {
            // Query the current CurrentReputationEntity for the current user
            Optional<CurrentReputationEntity> optionalCurrentReputationEntity = currentReputationRepository.findById(userOid);

            // Create a new entity if one didn't exist
            CurrentReputationEntity currentReputationEntity = optionalCurrentReputationEntity.orElseGet(() ->
                    CurrentReputationEntity.builder()
                            .userOid(userOid)
                            .build());

            // Calculate total score
            currentReputationEntity.setTotalScore(getTotalScore(currentReputationEntity));

            // Save
            currentReputationRepository.save(currentReputationEntity);
        });
    }

    int getTotalScore(CurrentReputationEntity currentReputationEntity) {
        long userOid = currentReputationEntity.getUserOid();

        double qualityAnalysisValue = qualityAnalysisComponent(userOid);
        currentReputationEntity.setQualityAnalysis(qualityAnalysisValue);

        boolean isConductStatusNegative = currentReputationEntity.isConductNegative();
        boolean isKycVerified = currentReputationEntity.isKycVerified();

        return TotalReputationCalculator.calculateTotalScore(qualityAnalysisValue, isConductStatusNegative, isKycVerified);
    }

    double qualityAnalysisComponent(long userOid) {
        BigDecimal followerQuality = getReputationScoreComponent(getFollowerQuality(userOid), FOLLOWERS_MULTIPLIER);
        BigDecimal contentQualityScore = getReputationScoreComponent(contentQualityCalculatorService.getContentQualityScoreForUser(userOid), POSTED_CONTENT_AND_COMMENTS_MULTIPLIER);
        BigDecimal voteCorrelationScore = getReputationScoreComponent(voteCorrelationService.getVoteCorrelationScoreForUser(userOid), CORRELATION_OF_VOTES_MULTIPLIER);
        BigDecimal ratingCorrelationScoreForUser = getReputationScoreComponent(ratingCorrelationService.getRatingCorrelationScoreForUser(userOid), CORRELATION_OF_RATINGS_MULTIPLIER);

        return followerQuality.add(contentQualityScore)
                .add(voteCorrelationScore)
                .add(ratingCorrelationScoreForUser).doubleValue();
    }

    double getFollowerQuality(long userOid) {
        FollowerQualityEntity followerQualityEntity = followerQualityRepository.findById(userOid).orElse(null);
        double followerQuality;
        if (followerQualityEntity == null) {
            followerQuality = 0;
        } else {
            followerQuality = followerQualityEntity.getUserQualityFollowerPctRank();
        }

        return followerQuality;
    }

    private static BigDecimal getReputationScoreComponent(double scoreValue, BigDecimal multiplier) {
        return BigDecimal.valueOf(scoreValue).multiply(multiplier);
    }
}
