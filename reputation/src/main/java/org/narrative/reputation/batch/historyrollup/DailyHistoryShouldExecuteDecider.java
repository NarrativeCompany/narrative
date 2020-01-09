package org.narrative.reputation.batch.historyrollup;

import org.narrative.batch.service.BatchJobControlService;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ConsolidatedCurReputationRepository;
import org.narrative.reputation.repository.ReputationHistoryRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static org.narrative.reputation.config.batch.HistoryRollupJobConfig.*;

/**
 * {@link JobExecutionDecider} to determine if daily history should execute
 */
@Component
public class DailyHistoryShouldExecuteDecider extends HistoryStepShouldExecuteDecider {
    private final ConsolidatedCurReputationRepository consolidatedCurReputationRepository;

    public DailyHistoryShouldExecuteDecider(BatchJobControlService batchJobControlService, ReputationHistoryRepository reputationHistoryRepository, ReputationProperties reputationProperties,
                                            ConsolidatedCurReputationRepository consolidatedCurReputationRepository, DailyHistoryRollupTasklet dailyHistoryRollupTasklet) {
        super(batchJobControlService, reputationHistoryRepository, DAILY_HISTORY_ROLLUP_STEP, reputationProperties.getDailyHistoryRollupJobProperties());
        this.consolidatedCurReputationRepository = consolidatedCurReputationRepository;
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // Convert now to LocalDate for comparison.  Subtract one day since the daily rollup data will be written with
        // the previous day's date when the job executes.
        LocalDate nowZonedDate = buildZoneAdjustedNowLocalDate().minusDays(1);
        LatestDataDate latestDataDate = findLatestDataDateForRollupPeriod(RollupPeriod.DAILY);

        boolean shouldExecute = nowZonedDate.isAfter(latestDataDate.getZoneAdjustedSuccessDate());

        // IIF the job should not execute (executed already for today), count source rows and compare to existing daily
        // rollup rows - if this row count does not match this is a recovery scenario so allow the job to run
        boolean dataIncompleteShouldExecute = false;
        if (!shouldExecute) {
            log.debug("Testing whether data is incomplete for step");

            // Count source rows and rollup rows
            long sourceCount = consolidatedCurReputationRepository.count();
            long destCount = reputationHistoryRepository.countByPeriodAndSnapshotDate(RollupPeriod.DAILY, latestDataDate.getLastSuccessDate());

            if (sourceCount > destCount) {
                dataIncompleteShouldExecute = true;
                log.debug("sourceCount {} > destCount {} - dataIncompleteShouldExecute == true", sourceCount, destCount);
            }

            log.debug("isDataIncomplete: source row count: {} Dest row count: {} dataIncompleteShouldExecute: {}", sourceCount, destCount, dataIncompleteShouldExecute);
        }

        log.debug("Comparing last successful date {} to now {} dataIncompleteShouldExecute: {}", latestDataDate.getZoneAdjustedSuccessDate(), nowZonedDate, dataIncompleteShouldExecute);

        if (shouldExecute) {
            log.debug("Comparison is > 1 day - will execute step");
            return new FlowExecutionStatus(CONTINUE_STEP);
        } else if (dataIncompleteShouldExecute) {
            log.debug("Comparison is <= 1 day but data is incomplete - will execute step");
            return new FlowExecutionStatus(CONTINUE_STEP);
        } else {
            log.debug("Comparison is <= 1 day - will not execute step");
            return new FlowExecutionStatus(NEXT_STEP);
        }
    }
}
