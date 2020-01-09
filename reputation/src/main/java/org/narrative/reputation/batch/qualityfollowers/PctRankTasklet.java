package org.narrative.reputation.batch.qualityfollowers;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.repository.FollowerQualityRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Tasklet to calculate percentile rank for follower quality
 */
@Slf4j
@Component
public class PctRankTasklet implements Tasklet {
    private final FollowerQualityRepository followerQualityRepository;

    public PctRankTasklet(FollowerQualityRepository followerQualityRepository) {
        this.followerQualityRepository = followerQualityRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int rowsProcessed = followerQualityRepository.updateUserQualityPercentileRank();

        log.info("{} rows processed", rowsProcessed);

        return RepeatStatus.FINISHED;
    }
}
