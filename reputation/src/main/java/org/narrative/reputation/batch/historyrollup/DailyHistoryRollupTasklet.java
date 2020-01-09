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
public class DailyHistoryRollupTasklet extends RollupTasklet {
    public DailyHistoryRollupTasklet(ReputationHistoryRepository reputationHistoryRepository, ReputationProperties reputationProperties) {
        super(reputationHistoryRepository, reputationProperties, RollupPeriod.DAILY, RollupPeriod.DAILY, LoggerFactory.getLogger(DailyHistoryRollupTasklet.class));
    }

    @Override
    protected LocalDate calculateRollupDate() {
        LocalDate now = getNow();
        // When we get to this step, we already know we should create a snapshot of the previous day's data
        return now.minusDays(1);
    }

    @Override
    protected LocalDate getOrCalculateRollupDate(ChunkContext chunkContext) {
        //Calculate the target rollup date if not in the context
        LocalDate rollupDate = (LocalDate) chunkContext.getStepContext().getAttribute(ROLLUP_DATE);
        if (rollupDate == null) {
            // When we get to this step, we already know we should create a snapshot of the previous day's data
            rollupDate = calculateRollupDate();
            chunkContext.getStepContext().setAttribute(ROLLUP_DATE, rollupDate);
        }

        return rollupDate;
    }

    @Override
    protected LocalDate getOrCalculateSourceDataDate(ChunkContext chunkContext, LocalDate rollupDate) {
        return rollupDate.minusDays(1);
    }

    @Override
    protected int performHistoryBatchInsert(LocalDate sourceDataDate, LocalDate rollupDate, long maxUserOidForPrevBatch) {
        return reputationHistoryRepository.insertCurrentReputationBatchIntoHistory(chunkSize, rollupDate, RollupPeriod.DAILY.getOrdinalValue(), maxUserOidForPrevBatch);
    }
}
