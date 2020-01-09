package org.narrative.reputation.batch.historyrollup;

import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ReputationHistoryRepository;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDate;

/**
 * Base task with shared functionality for daily/weekly/monthly rollup Tasklets
 */
public abstract class RollupTasklet implements Tasklet {
    static final String ROLLUP_DATE = "rollupDate";
    static final String SOURCE_DATA_DATE = "sourceDataDate";
    static final String INSERTED_COUNT = "insertedCount";

    final ReputationHistoryRepository reputationHistoryRepository;
    final int chunkSize;
    final RollupPeriod rollupPeriod;
    final RollupPeriod sourceRollupPeriod;
    final Logger log;
    final String taskName;

    public RollupTasklet(ReputationHistoryRepository reputationHistoryRepository, ReputationProperties reputationProperties, RollupPeriod rollupPeriod, RollupPeriod sourceRollupPeriod, Logger log) {
        this.reputationHistoryRepository = reputationHistoryRepository;
        chunkSize = reputationProperties.getDailyHistoryRollupJobProperties().getChunkSize();
        this.rollupPeriod = rollupPeriod;
        this.sourceRollupPeriod = sourceRollupPeriod;
        this.log = log;
        this.taskName = getClass().getSimpleName();
    }

    /**
     * Get the current {@link LocalDate}
     */
    protected LocalDate getNow() {
        return LocalDate.now();
    }

    /**
     * Calculate the rollup date for this step
     */
    protected abstract LocalDate calculateRollupDate();

    /**
     * Get or calculate the rollup date for this step
     */
    protected abstract LocalDate getOrCalculateRollupDate(ChunkContext chunkContext);

    /**
     * Calculate the source data date for this step
     */
    protected LocalDate calculateSourceDataDate(LocalDate rollupDate) {
        return reputationHistoryRepository.findSourceDateOfTypeLessThanOrEqualCompareDate(sourceRollupPeriod, rollupDate);
    }

    /**
     * Get or calculate the source data date for this step
     */
    protected LocalDate getOrCalculateSourceDataDate(ChunkContext chunkContext, LocalDate rollupDate) {
        // Find the first rollup source date of type sourceRollupPeriod that is less than our period rollup date - this is our source data
        LocalDate sourceDataDate = (LocalDate) chunkContext.getStepContext().getAttribute(SOURCE_DATA_DATE);
        if (sourceDataDate == null) {
            sourceDataDate = calculateSourceDataDate(rollupDate);
            chunkContext.getStepContext().setAttribute(SOURCE_DATA_DATE, sourceDataDate);
        }

        return sourceDataDate;
    }

    /**
     * Insert a batch of rows into the history
     *
     * @return Number of rows processed
     */
    protected abstract int performHistoryBatchInsert(LocalDate sourceDataDate, LocalDate rollupDate, long maxUserOidForPrevBatch);

    /**
     * Shared execute to iterate and perform batch inserts
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate rollupDate = getOrCalculateRollupDate(chunkContext);
        LocalDate sourceDataDate = getOrCalculateSourceDataDate(chunkContext, rollupDate);

        // Keep track of how much we have processed
        Integer totalInsertedCount = (Integer) chunkContext.getStepContext().getAttribute(INSERTED_COUNT);
        if (totalInsertedCount == null) {
            totalInsertedCount = 0;
        }

        // Find the max user oid for this job
        Long maxUserOidForPrevBatch = reputationHistoryRepository.findMaxUserOidForRollupPeriodAndDate(rollupPeriod, rollupDate);
        if (maxUserOidForPrevBatch == null) {
            maxUserOidForPrevBatch = 0L;
        }

        log.info("Attempting history batch insert for rollupDate {} and sourceDate {} with maxUserOidForPrevBatch {}", rollupDate, sourceDataDate, maxUserOidForPrevBatch);

        int rowsInserted = 0;
        int totalInsertedSum = 0;
        if (sourceDataDate != null) {
            // Do the insert batch
            rowsInserted = performHistoryBatchInsert(sourceDataDate, rollupDate, maxUserOidForPrevBatch);

                    totalInsertedSum = rowsInserted + totalInsertedCount;
            log.info("{}: {} rows inserted into history - total inserted: {}", taskName, rowsInserted, totalInsertedSum);
        } else {
            log.info("{}: no source data found - nothing to do", taskName);
        }

        // If rows inserted is zero then we're done
        if (rowsInserted == 0) {
            return RepeatStatus.FINISHED;
        } else {
            // Continue - execute inserts until we have nothing left to do
            chunkContext.getStepContext().setAttribute(INSERTED_COUNT, totalInsertedSum);
            return RepeatStatus.CONTINUABLE;
        }
    }
}
