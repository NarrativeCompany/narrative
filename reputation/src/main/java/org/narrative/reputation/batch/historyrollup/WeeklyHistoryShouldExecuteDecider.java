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

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.narrative.reputation.config.batch.HistoryRollupJobConfig.*;

/**
 * {@link JobExecutionDecider} to determine if daily history should execute
 */
@Component
public class WeeklyHistoryShouldExecuteDecider extends HistoryToHistoryStepShouldExecuteDecider {
    private final DayOfWeek firstDayOfWeek;

    public WeeklyHistoryShouldExecuteDecider(BatchJobControlService batchJobControlService, ReputationProperties reputationProperties, ReputationHistoryRepository reputationHistoryRepository, WeeklyHistoryRollupTasklet weeklyHistoryRollupTasklet) {
        super(batchJobControlService, reputationHistoryRepository, WEEKLY_HISTORY_ROLLUP_STEP, reputationProperties.getWeeklyHistoryRollupJobProperties(), weeklyHistoryRollupTasklet);
        firstDayOfWeek = reputationProperties.getFirstDayOfWeek();
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // Convert to LocalDate for comparison
        LocalDate nowZonedDate = buildZoneAdjustedNowLocalDate();

        // Find the latest data date for the weekly rollup type
        LatestDataDate latestDataDate = findLatestDataDateForRollupPeriod(RollupPeriod.WEEKLY);

        boolean dayIsFirstDayOfWeek = nowZonedDate.getDayOfWeek().equals(firstDayOfWeek);
        boolean dateGreaterThan7Days = nowZonedDate.isAfter(latestDataDate.getZoneAdjustedSuccessDate().plusDays(7));

        // IIF the job should not execute (executed already for today), count source rows and compare to existing daily
        // rollup rows - if this row count does not match this is a recovery scenario so allow the job to run
        boolean dataIncompleteShouldExecute = false;
        if (dayIsFirstDayOfWeek && !dateGreaterThan7Days) {
            dataIncompleteShouldExecute = isDataIncomplete(RollupPeriod.DAILY, RollupPeriod.WEEKLY);
        }

        log.debug("First day of week: {} Last successful date check success: {} compared {} to now {} dataIncompleteShouldExecute: {}", dayIsFirstDayOfWeek, dateGreaterThan7Days, latestDataDate.getZoneAdjustedSuccessDate(), nowZonedDate, dataIncompleteShouldExecute);

        if (dayIsFirstDayOfWeek && dateGreaterThan7Days) {
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
