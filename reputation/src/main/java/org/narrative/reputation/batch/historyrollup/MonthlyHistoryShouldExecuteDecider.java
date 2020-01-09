package org.narrative.reputation.batch.historyrollup;

import org.narrative.batch.service.BatchJobControlService;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
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
public class MonthlyHistoryShouldExecuteDecider extends HistoryToHistoryStepShouldExecuteDecider {
    public MonthlyHistoryShouldExecuteDecider(BatchJobControlService batchJobControlService, ReputationProperties reputationProperties, ReputationHistoryRepository reputationHistoryRepository, MonthlyHistoryRollupTasklet monthlyHistoryRollupTasklet) {
        super(batchJobControlService, reputationHistoryRepository, MONTHLY_HISTORY_ROLLUP_STEP, reputationProperties.getWeeklyHistoryRollupJobProperties(), monthlyHistoryRollupTasklet);
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // Convert to LocalDate for comparison
        LocalDate nowZonedDate = buildZoneAdjustedNowLocalDate();

        // Find the latest data date for the weekly rollup type
        LatestDataDate latestDataDate = findLatestDataDateForRollupPeriod(RollupPeriod.MONTHLY);

        // Find the length in days of the previous month
        int daysOfPreviousMonth = nowZonedDate.minusMonths(1).lengthOfMonth();
        // Is now greater than the last execution date + days of the previous month?
        boolean dateGreaterThanLastMonthDays = nowZonedDate.isAfter(latestDataDate.getZoneAdjustedSuccessDate().plusDays(daysOfPreviousMonth));

        boolean dayIsFirstDayOfMonth = nowZonedDate.getDayOfMonth() == 1;

        // IIF the job should not execute (executed already for today), count source rows and compare to existing daily
        // rollup rows - if this row count does not match this is a recovery scenario so allow the job to run
        boolean dataIncompleteShouldExecute = false;
        if (dayIsFirstDayOfMonth && !dateGreaterThanLastMonthDays) {
            dataIncompleteShouldExecute = isDataIncomplete(RollupPeriod.WEEKLY, RollupPeriod.MONTHLY);
        }

        log.debug("First day of month: {} Last successful date check success: {} compared {} to now {} dataIncompleteShouldExecute: {}", dayIsFirstDayOfMonth, dateGreaterThanLastMonthDays, latestDataDate.getZoneAdjustedSuccessDate(), nowZonedDate, dataIncompleteShouldExecute);

        if (dayIsFirstDayOfMonth && dateGreaterThanLastMonthDays) {
            log.debug("Checks successful - will execute step");
            return new FlowExecutionStatus(CONTINUE_STEP);
        } else if (dataIncompleteShouldExecute) {
            log.debug("Data incomplete from last execution - will execute step");
            return new FlowExecutionStatus(CONTINUE_STEP);
        } else {
            log.debug("Will not execute step");
            return new FlowExecutionStatus(NEXT_STEP);
        }
    }
}
