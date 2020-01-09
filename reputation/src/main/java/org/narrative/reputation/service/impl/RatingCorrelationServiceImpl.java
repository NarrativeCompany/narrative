package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.model.entity.RatingCorrelationEntity;
import org.narrative.reputation.repository.RatingCorrelationRepository;
import org.narrative.reputation.service.RatingCorrelationService;
import org.narrative.shared.event.reputation.ConsensusChangedEvent;
import org.narrative.shared.event.reputation.RatingEvent;
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
public class RatingCorrelationServiceImpl implements RatingCorrelationService {

    private final RatingCorrelationRepository ratingCorrelationRepository;

    @Autowired
    public RatingCorrelationServiceImpl(RatingCorrelationRepository ratingCorrelationRepository) {this.ratingCorrelationRepository = ratingCorrelationRepository;}

    @Override
    public RatingEvent updateRatingCorrelationWithRatingEvent(RatingEvent ratingEvent) {
        log.info("updateRatingCorrelationWithRatingEvent with: {} " , ratingEvent);

        // Query the current RatingCorrelationEntity for the current user
        Optional<RatingCorrelationEntity> optionalRatingCorrelationEntity = ratingCorrelationRepository.findById(ratingEvent.getUserOid());

        // Create a new entity if one didn't exist
        RatingCorrelationEntity ratingCorrelationEntity = optionalRatingCorrelationEntity.orElseGet(() ->
                RatingCorrelationEntity.builder()
                        .userOid(ratingEvent.getUserOid())
                        .build());

        // Set metadata
        ratingCorrelationEntity.setLastEventId(ratingEvent.getEventId());
        ratingCorrelationEntity.setLastEventTimestamp(ratingEvent.getEventTimestamp());

        // Calculate the new values based on event type and persist
        if(ratingEvent.isRemoveVote()) {
            // bl: if the user removed the vote, then decrement the total vote count
            ratingCorrelationEntity.setTotalVoteCount(ratingCorrelationEntity.getTotalVoteCount() - 1);
            // bl: if the user was in consensus previously, then also decrement the majority vote count
            if(ratingEvent.isWasRatedWithConsensus()) {
                ratingCorrelationEntity.setMajorityVoteCount(ratingCorrelationEntity.getMajorityVoteCount() - 1);
            }
        } else if (ratingEvent.isRevote()) {
            // bl: if it's a revote, don't touch total vote count. but we may need to change the majority vote count
            // if the user's vote with respect to consensus changed.
            if (ratingEvent.isWasRatedWithConsensus() != ratingEvent.isRatedWithConsensus()) {
                // if you were previously in consensus, but now you are not, we need to decrement the majority vote count
                // if you were previously out of consensus, but now you are in consensus, we need to increment the majority vote count
                int adjustment = ratingEvent.isWasRatedWithConsensus() ? -1 : 1;
                ratingCorrelationEntity.setMajorityVoteCount(ratingCorrelationEntity.getMajorityVoteCount() + adjustment);
            }
        } else {
            // If not a re-vote, increment totalVoteCount
            ratingCorrelationEntity.setTotalVoteCount(ratingCorrelationEntity.getTotalVoteCount() + 1);
            // If ratedWithConsensus, increment majorityVoteCount
            if (ratingEvent.isRatedWithConsensus()) {
                ratingCorrelationEntity.setMajorityVoteCount(ratingCorrelationEntity.getMajorityVoteCount() + 1);
            }
        }

        ratingCorrelationRepository.save(ratingCorrelationEntity);
        return ratingEvent;
    }

    @Override
    public ConsensusChangedEvent updateRatingCorrelationWithRatingConsensusChangedEvent(ConsensusChangedEvent consensusChangedEvent) {
        log.info("updateRatingCorrelationWithRatingConsensusChangedEvent with: {} " , consensusChangedEvent);

        // For each user in the map
        consensusChangedEvent.getUsersConsensusChangedMap().forEach((userOid, nowAgreesWithConsensus) -> {
            // Query the current RatingCorrelationEntity
            Optional<RatingCorrelationEntity> optionalRatingCorrelationEntity = ratingCorrelationRepository.findById(userOid);

            // Create a new entity if one didn't exist
            RatingCorrelationEntity ratingCorrelationEntity = optionalRatingCorrelationEntity.orElseGet(() ->
                    RatingCorrelationEntity.builder()
                            .userOid(userOid)
                            .build());

            // Set metadata
            ratingCorrelationEntity.setLastEventId(consensusChangedEvent.getEventId());
            ratingCorrelationEntity.setLastEventTimestamp(consensusChangedEvent.getEventTimestamp());

            //If the boolean value for the current user is true, increment majorityVoteCount.
            if (nowAgreesWithConsensus) {
                ratingCorrelationEntity.setMajorityVoteCount(ratingCorrelationEntity.getMajorityVoteCount() + 1);
            } else {
                // If false, decrement majorityVoteCount
                ratingCorrelationEntity.setMajorityVoteCount(ratingCorrelationEntity.getMajorityVoteCount() - 1);
            }

            // Persist the modified entity
            ratingCorrelationRepository.save(ratingCorrelationEntity);
        });

        return consensusChangedEvent;
    }

    @Override
    @Transactional(readOnly = true)
    public double getRatingCorrelationScoreForUser(long userOid) {
        log.info("getRatingCorrelationScoreForUser with: {} " , userOid);

        // Query the current RatingCorrelationEntity for the current user and throw EntityNotFoundException if the user is not found
        RatingCorrelationEntity ratingCorrelationEntity = ratingCorrelationRepository.findById(userOid).orElse(null);

        if (ratingCorrelationEntity == null || ratingCorrelationEntity.getTotalVoteCount() == 0) {
            return 0;
        }

        // Calculate grossQualityValue
        double grossQualityValue = (double) ratingCorrelationEntity.getMajorityVoteCount() / (double) ratingCorrelationEntity.getTotalVoteCount();

        // Get valueMultiplier
        double valueMultiplier = ContentQualityCalculatorServiceImpl.getQualityValueMultiplier(ratingCorrelationEntity.getTotalVoteCount());

        // Return net quality value
        return grossQualityValue * valueMultiplier * 100;

    }
}
