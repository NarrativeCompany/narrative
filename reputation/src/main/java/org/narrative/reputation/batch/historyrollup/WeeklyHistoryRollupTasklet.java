package org.narrative.reputation.batch.historyrollup;

import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ReputationHistoryRepository;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Tasklet to roll up daily history
 */
@Component
public class WeeklyHistoryRollupTasklet extends RollupTasklet {
    private final DayOfWeek firstDayOfWeek;

    public WeeklyHistoryRollupTasklet(ReputationHistoryRepository reputationHistoryRepository, ReputationProperties reputationProperties) {
        super(reputationHistoryRepository, reputationProperties, RollupPeriod.WEEKLY, RollupPeriod.DAILY, LoggerFactory.getLogger(WeeklyHistoryRollupTasklet.class));
        firstDayOfWeek = reputationProperties.getFirstDayOfWeek();
    }

    @Override
    protected LocalDate calculateRollupDate() {
        // When we get to this step, we already know we should create a snapshot of the last day of previous week's data
        // Find the number of days since the beginning of the week.  Offset now by this difference to get the
        // last day of the last week.
        LocalDate now = getNow();
        DayOfWeek curDayOfWeek = DayOfWeek.from(now);
        int offsetFromFirstDay = curDayOfWeek.ordinal() - firstDayOfWeek.ordinal();
        return now.minusDays(offsetFromFirstDay + 1);
    }

    @Override
    protected LocalDate getOrCalculateRollupDate(ChunkContext chunkContext) {
        //Calculate the target rollup date if not in the context
        LocalDate rollupDate = (LocalDate) chunkContext.getStepContext().getAttribute(ROLLUP_DATE);
        if (rollupDate == null) {
            rollupDate = calculateRollupDate();
            chunkContext.getStepContext().setAttribute(ROLLUP_DATE, rollupDate);
        }

        return rollupDate;
    }

    @Override
    protected int performHistoryBatchInsert(LocalDate sourceDataDate, LocalDate rollupDate, long maxUserOidForPrevBatch) {
        return reputationHistoryRepository.insertHistoryByDateAndTypeIntoIntoHistory(chunkSize, sourceDataDate, RollupPeriod.DAILY.getOrdinalValue(), rollupDate, RollupPeriod.WEEKLY.getOrdinalValue(), maxUserOidForPrevBatch);
    }
}
