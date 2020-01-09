package org.narrative.reputation.batch.qualityfollowers;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.CurrentQualityMembersEntity;
import org.narrative.reputation.repository.CurrentQualityMembersRepository;
import org.narrative.reputation.repository.FollowerQualityRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import static org.narrative.batch.util.BatchJobHelper.*;

/**
 * Tasklet to update the User Follower Quality Ratio for members
 */
@Slf4j
@Component
public class UQFRTasklet implements Tasklet {
    public static final String PROCESSED_COUNT = "processedCount";

    private final CurrentQualityMembersRepository currentQualityMembersRepository;
    private final FollowerQualityRepository followerQualityRepository;
    private final int chunkSize;

    public UQFRTasklet(CurrentQualityMembersRepository currentQualityMembersRepository, FollowerQualityRepository followerQualityRepository, ReputationProperties reputationProperties) {
        this.currentQualityMembersRepository = currentQualityMembersRepository;
        this.followerQualityRepository = followerQualityRepository;
        this.chunkSize = reputationProperties.getUserQualityFollowersRatioChunkSize();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Keep track of how much we have processed
        Long processedCount = (Long) chunkContext.getStepContext().getAttribute(PROCESSED_COUNT);
        if (processedCount == null) {
            processedCount = 0L;
        }

        // Extract the mean rep score and TQM  - calculated by previous step
        CurrentQualityMembersEntity currentQualityMembersEntity = currentQualityMembersRepository.getOne(1);
        double meanRepScore = currentQualityMembersEntity.getMeanReputationScore();
        long tqm = currentQualityMembersEntity.getTotalQualityMembers();

        // Use the batch execution id as the batch id - this is a timestamp so will always increase
        long jobId = (long) chunkContext.getStepContext().getJobParameters().get(JOB_UNIQUE_ID_KEY);

        // Find the max user oid for this job
        long maxUserOidForBatch = followerQualityRepository.findMaxUserOidForBatchJobId(jobId);

        log.info("Processing chunk of up to {} users starting with userOid {} for jobId: {} - total users processed so far: {}", chunkSize, maxUserOidForBatch, jobId, processedCount);

        // Execute an update for this chunk - all previously updated rows will have this job id.  The underlying query
        // filters these out and processes #chunkSize rows
        int updateCount = followerQualityRepository.updateUserQualityFollowerRatio(maxUserOidForBatch, jobId, chunkSize, meanRepScore, tqm);

        log.info("updateUserQualityFollowerRatio: {} rows processed" , updateCount);

        // Update all users for this chunk that previously had a follower quality and now do not
        int noQUpdateCount = followerQualityRepository.updateNoQualityFollowerUsers(maxUserOidForBatch, jobId, chunkSize);

        log.info("updateNoQualityFollowerUsers: {} rows processed" , noQUpdateCount);

        // If both update count and noQ update count are zero, nothing left to do - we're done
        if (updateCount == 0 && noQUpdateCount == 0) {
            return RepeatStatus.FINISHED;
        } else {
            chunkContext.getStepContext().setAttribute(PROCESSED_COUNT, processedCount + updateCount);
            return RepeatStatus.CONTINUABLE;
        }
    }
}



