package org.narrative.reputation.batch.qualityfollowers;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.model.entity.CurrentQualityMembersEntity;
import org.narrative.reputation.repository.CurrentQualityMembersRepository;
import org.narrative.reputation.repository.CurrentReputationRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Tasklet to calculate mean reputation score and total quality members
 */
@Slf4j
@Component
public class TQMTasklet implements Tasklet {
    private final CurrentReputationRepository currentReputationRepository;
    private final CurrentQualityMembersRepository currentQualityMembersRepository;

    public TQMTasklet(CurrentReputationRepository currentReputationRepository, CurrentQualityMembersRepository currentQualityMembersEntity) {
        this.currentReputationRepository = currentReputationRepository;
        this.currentQualityMembersRepository = currentQualityMembersEntity;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        double meanRepScore = currentReputationRepository.calculateMeanReputationScore();
        long tqm = currentReputationRepository.countByReputationScoreGreaterThanEqual(meanRepScore);

        CurrentQualityMembersEntity entity =
                CurrentQualityMembersEntity.builder()
                        .id(1)
                        .lastUpdated(Instant.now())
                        .meanReputationScore(meanRepScore)
                        .totalQualityMembers(tqm)
                        .build();

        currentQualityMembersRepository.save(entity);

        log.info("Calculated mean reputation score: {} total quality members: {}", meanRepScore, tqm);

        return RepeatStatus.FINISHED;
    }
}
