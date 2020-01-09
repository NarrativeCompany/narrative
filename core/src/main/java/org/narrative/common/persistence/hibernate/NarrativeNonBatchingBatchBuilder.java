package org.narrative.common.persistence.hibernate;

import org.hibernate.engine.jdbc.batch.internal.BatchBuilderImpl;
import org.hibernate.engine.jdbc.batch.internal.BatchingBatch;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;

/**
 * Date: 2/16/16
 * Time: 12:11 PM
 *
 * @author brian
 */
public class NarrativeNonBatchingBatchBuilder extends BatchBuilderImpl {
    @Override
    public Batch buildBatch(BatchKey key, JdbcCoordinator jdbcCoordinator) {
        final Integer sessionJdbcBatchSize = jdbcCoordinator.getJdbcSessionOwner()
                .getJdbcBatchSize();
        final int jdbcBatchSizeToUse = sessionJdbcBatchSize == null ?
                this.getJdbcBatchSize() :
                sessionJdbcBatchSize;
        return jdbcBatchSizeToUse > 1
                ? new BatchingBatch( key, jdbcCoordinator, jdbcBatchSizeToUse )
                : new NarrativeNonBatchingBatch( key, jdbcCoordinator );
    }
}
