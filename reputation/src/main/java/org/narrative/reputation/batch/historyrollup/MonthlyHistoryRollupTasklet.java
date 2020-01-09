package org.narrative.reputation.batch.historyrollup;

import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ReputationHistoryRepository;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Tasklet to roll up daily history
 */
@Component
public class MonthlyHistoryRollupTasklet extends RollupTasklet {
    public MonthlyHistoryRollupTasklet(ReputationHistoryRepository reputationHistoryRepository, ReputationProperties reputationProperties) {
        super(reputationHistoryRepository, reputationProperties, RollupPeriod.MONTHLY, RollupPeriod.WEEKLY, LoggerFactory.getLogger(MonthlyHistoryRollupTasklet.class));
    }

    @Override
    protected LocalDate calculateRollupDate() {
        // When we get to this step, we already know we should create a snapshot of the last weekly rollup
        return getNow().withDayOfMonth(1);
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
        return reputationHistoryRepository.insertHistoryByDateAndTypeIntoIntoHistory(chunkSize, sourceDataDate, RollupPeriod.WEEKLY.getOrdinalValue(), rollupDate, RollupPeriod.MONTHLY.getOrdinalValue(), maxUserOidForPrevBatch);
    }
}
