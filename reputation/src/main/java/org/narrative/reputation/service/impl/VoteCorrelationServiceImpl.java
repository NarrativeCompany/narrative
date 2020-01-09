package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.model.entity.VoteCorrelationEntity;
import org.narrative.reputation.repository.VoteCorrelationRepository;
import org.narrative.reputation.service.VoteCorrelationService;
import org.narrative.shared.event.reputation.NegativeQualityEvent;
import org.narrative.shared.event.reputation.ReputationEvent;
import org.narrative.shared.event.reputation.VoteEndedEvent;
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
public class VoteCorrelationServiceImpl implements VoteCorrelationService {
    private final VoteCorrelationRepository voteCorrelationRepository;

    @Autowired
    public VoteCorrelationServiceImpl(VoteCorrelationRepository voteCorrelationRepository) {this.voteCorrelationRepository = voteCorrelationRepository;}

    private VoteCorrelationEntity findAndPrepareEntity(long userOid, ReputationEvent event) {
        Optional<VoteCorrelationEntity> optionalVoteCorrelationEntity = voteCorrelationRepository.findById(userOid);

        // Create a new entity if one didn't exist
        VoteCorrelationEntity voteCorrelationEntity = optionalVoteCorrelationEntity.orElseGet(() ->
                VoteCorrelationEntity.builder()
                        .userOid(userOid)
                        .build());

        // Set metadata
        voteCorrelationEntity.setLastEventId(event.getEventId());
        voteCorrelationEntity.setLastEventTimestamp(event.getEventTimestamp());

        return voteCorrelationEntity;
    }

    @Override
    public VoteEndedEvent updateVoteCorrelationWithEvent(VoteEndedEvent voteEndedEvent) {
        log.info("updateVoteCorrelationWithEvent with: {} " , voteEndedEvent);

        // For each user in the map
        voteEndedEvent.getUserVotesMap().forEach((userOid, vote) -> {
            VoteCorrelationEntity voteCorrelationEntity = findAndPrepareEntity(userOid, voteEndedEvent);

            // Increment the totalVoteCount
            voteCorrelationEntity.setTotalVoteCount(voteCorrelationEntity.getTotalVoteCount() + 1);

            // If the user’s DecisionEnum value matches the decision, increment the majorityVoteCount
            if (vote == voteEndedEvent.getDecision()) {
                voteCorrelationEntity.setMajorityVoteCount(voteCorrelationEntity.getMajorityVoteCount() + 1);
            }

            // Persist the modified VoteCorrelationEntity
            voteCorrelationRepository.save(voteCorrelationEntity);
        });

        return voteEndedEvent;
    }

    @Override
    public NegativeQualityEvent updateVoteCorrelationForNegativeEvent(NegativeQualityEvent negativeQualityEvent) {
        log.info("updateVoteCorrelationForNegativeEvent with: {} " , negativeQualityEvent);

        // Query the current VoteCorrelationEntity
        long userOid = negativeQualityEvent.getUserOid();
        VoteCorrelationEntity voteCorrelationEntity = findAndPrepareEntity(userOid, negativeQualityEvent);

        // Calculate and issue the penalty
        int penalty = negativeQualityEvent.getNegativeQualityEventType().getPenalty();

        // Prefer adjusting the majority vote count down by the penalty amount unless it is less than the penalty amount.
        // In that case, adjust majority vote count down to zero
        int majorityVoteCountAdjustment = Math.min(voteCorrelationEntity.getMajorityVoteCount(), penalty);
        voteCorrelationEntity.setMajorityVoteCount(voteCorrelationEntity.getMajorityVoteCount() - majorityVoteCountAdjustment);

        // Adjust the total vote count up by the remainder of the penalty amount.  This adjustment will be zero if the
        // user's majority vote count >= the penalty amount
        int totalVoteCountAdjustment = penalty - majorityVoteCountAdjustment;
        voteCorrelationEntity.setTotalVoteCount(voteCorrelationEntity.getTotalVoteCount() + totalVoteCountAdjustment);

        // Persist the modified VoteCorrelationEntity
        voteCorrelationRepository.save(voteCorrelationEntity);

        return negativeQualityEvent;
    }

    @Override
    @Transactional(readOnly = true)
    public double getVoteCorrelationScoreForUser(long userOid) {
        log.info("getVoteCorrelationScoreForUser with: {} " , userOid);


        // Query the current VoteCorrelationEntity for the current user and throw EntityNotFoundException if the user is not found
        VoteCorrelationEntity voteCorrelationEntity = voteCorrelationRepository.findById(userOid).orElse(null);

        if (voteCorrelationEntity==null || voteCorrelationEntity.getTotalVoteCount() == 0){
            return 0;
        }

        // Calculate grossQualityValue
        double grossQualityValue = (double) voteCorrelationEntity.getMajorityVoteCount() / (double) voteCorrelationEntity.getTotalVoteCount();

        // Get valueMultiplier
        double valueMultiplier = ContentQualityCalculatorServiceImpl.getQualityValueMultiplier(voteCorrelationEntity.getTotalVoteCount());

        // Return net quality value
        return grossQualityValue * valueMultiplier * 100;
    }
}
