package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.model.entity.CurrentReputationEntity;
import org.narrative.reputation.repository.CurrentReputationRepository;
import org.narrative.reputation.service.KYCVerificationService;
import org.narrative.shared.event.reputation.KYCVerificationEvent;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class KYCVerificationServiceImpl implements KYCVerificationService {

    private final CurrentReputationRepository currentReputationRepository;
    private final Clock clock;

    @Autowired
    public KYCVerificationServiceImpl(CurrentReputationRepository currentReputationRepository, Clock clock) {
        this.currentReputationRepository = currentReputationRepository;
        this.clock=clock;
    }

    @Override
    public KYCVerificationEvent updateCurrentReputationWithKYCVerificationEvent(KYCVerificationEvent kycVerificationEvent) {
        log.info("updateCurrentReputationWithKYCVerificationEvent with: {} " , kycVerificationEvent);

        Instant now = clock.instant();

        // Query the current CurrentReputationEntity for the current user
        Optional<CurrentReputationEntity> optionalCurrentReputationEntity = currentReputationRepository.findById(kycVerificationEvent.getUserOid());

        // Create a new entity if one didn't exist
        CurrentReputationEntity currentReputationEntity = optionalCurrentReputationEntity.orElseGet(() ->
                CurrentReputationEntity.builder()
                        .userOid(kycVerificationEvent.getUserOid())
                        .build());

        // Set metadata
        currentReputationEntity.setLastEventId(kycVerificationEvent.getEventId());
        currentReputationEntity.setLastEventTimestamp(kycVerificationEvent.getEventTimestamp());

        // Set the rep entity kyc verified and set the kycVerifiedTimestamp
        currentReputationEntity.setKycVerified(kycVerificationEvent.isVerified());
        currentReputationEntity.setKycVerifiedTimestamp(now);

        // Set Negative ConductExpiration Timestamp to the time when the user was KYC verified if the user is currently conduct negative
        if (currentReputationEntity.isConductNegative()) {
            currentReputationEntity.setNegativeConductExpirationTimestamp(now);
        }

        currentReputationRepository.save(currentReputationEntity);

        return kycVerificationEvent;
    }
}
