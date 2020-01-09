package org.narrative.reputation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.narrative.reputation.model.entity.ConductStatusEntity;
import org.narrative.reputation.model.entity.CurrentReputationEntity;
import org.narrative.reputation.repository.ConductStatusRepository;
import org.narrative.reputation.repository.CurrentReputationRepository;
import org.narrative.reputation.service.ConductStatusCalculatorService;
import org.narrative.shared.event.reputation.ConductStatusEvent;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class ConductStatusCalculatorServiceImpl implements ConductStatusCalculatorService {
    private static final double DATE_WEIGHT_EXPONENT_MULTIPLIER = -0.015;
    private static final double PENALTY_TIME_A = 1.153;
    private static final double PENALTY_TIME_B = -2.625;
    private static final double PENALTY_TIME_C = 2.5;
    private final static long DAY_IN_SECONDS = TimeUnit.SECONDS.convert(1L, TimeUnit.DAYS);

    private final ConductStatusRepository conductStatusRepository;
    private final CurrentReputationRepository currentReputationRepository;
    private final Clock clock;

    @Autowired
    public ConductStatusCalculatorServiceImpl(final ConductStatusRepository conductStatusRepository,
                                              final CurrentReputationRepository currentReputationRepository,
                                              final Clock clock) {
        this.conductStatusRepository = conductStatusRepository;
        this.currentReputationRepository = currentReputationRepository;
        this.clock = clock;
    }

    @Override
    public ConductStatusEvent calculateNegativeConductExpirationDate(final ConductStatusEvent conductStatusEvent) {
        // Write the ConductStatusEvent to the DB
        writeConductStatusEventToDB(conductStatusEvent);

        // Get all of the ConductStatusEntities for the user
        List<ConductStatusEntity> conductStatusEntities = getConductStatusEntitiesForUser(conductStatusEvent);

        // Calculate the sum of weighted severities
        double weightedSeveritySum = calculateWeightedSeveritySum(conductStatusEntities);

        // Calculate penalty time in days
        double penaltyTime = calculatePenaltyTime(weightedSeveritySum);

        // Apply penalty time to user
        applyPenaltyTimeToUser(conductStatusEvent.getUserOid(), penaltyTime, conductStatusEvent.getEventTimestamp());

        return conductStatusEvent;
    }

    protected void writeConductStatusEventToDB(final ConductStatusEvent conductStatusEvent) {
        ConductStatusEntity conductStatusEntity = ConductStatusEntity.builder()
                .userOid(conductStatusEvent.getUserOid())
                .conductEventType(conductStatusEvent.getConductEventType())
                .eventTimestamp(conductStatusEvent.getEventTimestamp())
                .eventId(conductStatusEvent.getEventId())
                .build();

        conductStatusRepository.save(conductStatusEntity);

    }

    protected List<ConductStatusEntity> getConductStatusEntitiesForUser(final ConductStatusEvent event) {
        long userOid = event.getUserOid();

        Optional<CurrentReputationEntity> optionalCurrentReputationEntity = currentReputationRepository.findById(userOid);
        CurrentReputationEntity currentReputationEntity;
        List<ConductStatusEntity> conductStatusEntities;

        if (!optionalCurrentReputationEntity.isPresent()) {
            currentReputationEntity = CurrentReputationEntity.builder()
                    .userOid(userOid)
                    .lastEventId(event.getEventId())
                    .lastEventTimestamp(event.getEventTimestamp())
                    .build();

            currentReputationRepository.save(currentReputationEntity);
        } else {
            currentReputationEntity = optionalCurrentReputationEntity.get();
        }

        if (currentReputationEntity.isKycVerified()) {
            // Your negative active actions are wiped clean on your KYC date. Negative events prior to your KYC date are not considered in future penalty calculations
            // If user is kyc certified, only get events since KYC certification date
            conductStatusEntities = conductStatusRepository.findByUserOidAndEventTimestampAfter(userOid, currentReputationEntity.getKycVerifiedTimestamp());

        } else {
            // else get all events for the user
            conductStatusEntities = conductStatusRepository.findByUserOid(userOid);
        }

        return conductStatusEntities;
    }

    protected double calculateWeightedSeveritySum(final List<ConductStatusEntity> conductStatusEntities) {

        Instant now = clock.instant();

        Optional<Double> weightedSeveritySum = conductStatusEntities
                .stream()
                .map(conductStatusEntity -> {
                    // Calculate delta t
                    long deltaT = Duration.between(conductStatusEntity.getEventTimestamp(), now).toDays();

                    // Calculate date weight
                    double dateWeight = Math.exp(DATE_WEIGHT_EXPONENT_MULTIPLIER * deltaT);

                    // Return weighted severity
                    return conductStatusEntity.getConductEventType().getSeverity() * dateWeight;

                })
                .reduce((ws1, ws2) -> ws1 + ws2);

        return weightedSeveritySum.orElse(0.0);
    }

    protected double calculatePenaltyTime(final double weightedSeveritySum) {
        // Limit penalty to 180 days
        return Math.min(PENALTY_TIME_A * Math.pow(weightedSeveritySum, 2) + PENALTY_TIME_B * weightedSeveritySum + PENALTY_TIME_C, 180);
    }

    protected void applyPenaltyTimeToUser(final long userOid, final double penaltyDays, final Instant eventTimestamp) {
        CurrentReputationEntity currentReputationEntity = currentReputationRepository.findById(userOid).get();

        double kycModifiedPenaltyDays = penaltyDays;
        final Instant now = clock.instant();

        // Only apply 1/2 of the penalty time if the user is KYC certified
        if (currentReputationEntity.isKycVerified()) {
            kycModifiedPenaltyDays = kycModifiedPenaltyDays / 2;
        }
        Duration penaltyDurationToApply = Duration.of(Math.round(kycModifiedPenaltyDays * DAY_IN_SECONDS), ChronoUnit.SECONDS);

        Instant negativeConductExpirationDate = currentReputationEntity.getNegativeConductExpirationTimestamp();

        // Add penalty time to CurrentReputation for the user if they are already penalized
        if (negativeConductExpirationDate != null && negativeConductExpirationDate.isAfter(now)) {
            // Limit to 180 days from now
            Instant sixMonthsFromNow = now.plus(180, ChronoUnit.DAYS);
            Instant proposedPenaltyEndTimestamp = negativeConductExpirationDate.plus(penaltyDurationToApply);

            if (proposedPenaltyEndTimestamp.isAfter(sixMonthsFromNow)) {
                currentReputationEntity.setNegativeConductExpirationTimestamp(sixMonthsFromNow);
            } else {
                currentReputationEntity.setNegativeConductExpirationTimestamp(proposedPenaltyEndTimestamp);
            }
        } else {
            // Else add penalty time to eventTimestanp
            currentReputationEntity.setNegativeConductExpirationTimestamp(eventTimestamp.plus(penaltyDurationToApply));

            // Set the setNegativeConductStartTimestamp
            currentReputationEntity.setNegativeConductStartTimestamp(eventTimestamp);
        }

        // Save currentReputationEntity
        currentReputationRepository.save(currentReputationEntity);
    }
}
