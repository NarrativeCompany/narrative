package org.narrative.reputation.batch.historyrollup;

import org.narrative.batch.service.BatchJobControlService;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.narrative.reputation.repository.ReputationHistoryRepository;

import java.time.LocalDate;

public abstract class HistoryToHistoryStepShouldExecuteDecider extends HistoryStepShouldExecuteDecider {
    protected final RollupTasklet rollupTasklet;

    public HistoryToHistoryStepShouldExecuteDecider(BatchJobControlService batchJobControlService, ReputationHistoryRepository reputationHistoryRepository, String stepName, ReputationProperties.HistoryJobProperties historyJobProperties, RollupTasklet rollupTasklet) {
        super(batchJobControlService, reputationHistoryRepository, stepName, historyJobProperties);
        this.rollupTasklet = rollupTasklet;
    }

    /**
     * Determine whether rolled up data is incomplete for this tasklet.  This is used to determine whether step
     * continuation should be attempted
     */
    protected boolean isDataIncomplete(RollupPeriod sourceRollupPeriod, RollupPeriod destRollupPeriod) {
        // Calculate the rollup date
        LocalDate rollupDate = rollupTasklet.calculateRollupDate();
        // Calculate the source data date
        LocalDate sourceDataDate = rollupTasklet.calculateSourceDataDate(rollupDate);

        // Count source rows and rollup rows
        long sourceCount = reputationHistoryRepository.countByPeriodAndSnapshotDate(sourceRollupPeriod, sourceDataDate);
        long destCount = reputationHistoryRepository.countByPeriodAndSnapshotDate(destRollupPeriod, rollupDate);

        boolean dataIncompleteShouldExecute = false;
        if (sourceCount > destCount) {
            dataIncompleteShouldExecute = true;
            log.debug("sourceCount {} > destCount {} - dataIncompleteShouldExecute == true", sourceCount, destCount);
        }

        log.debug("isDataIncomplete: source row count: {} Dest row count: {} dataIncompleteShouldExecute: {}", sourceCount, destCount, dataIncompleteShouldExecute);
        return dataIncompleteShouldExecute;
    }
}
