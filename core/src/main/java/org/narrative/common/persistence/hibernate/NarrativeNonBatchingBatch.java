package org.narrative.common.persistence.hibernate;

import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.batch.internal.NonBatchingBatch;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * Date: 2/16/16
 * Time: 12:11 PM
 *
 * @author brian
 */
public class NarrativeNonBatchingBatch extends NonBatchingBatch {
    private final JdbcCoordinator scJdbcCoordinatorRef;

    protected NarrativeNonBatchingBatch(BatchKey key, JdbcCoordinator jdbcCoordinator) {
        super(key, jdbcCoordinator);
        scJdbcCoordinatorRef = jdbcCoordinator;
    }

    /**
     * Slightly refactored version of the superclass method to insert parameter logging for prepared statements when possible
     */
    @Override
    public void addToBatch() {
        notifyObserversImplicitExecution();
        for ( Map.Entry<String, PreparedStatement> entry : getStatements().entrySet() ) {
            final PreparedStatement statement = entry.getValue();
            try {
                final int rowCount = scJdbcCoordinatorRef.getResultSetReturn().executeUpdate( statement );
                getKey().getExpectation().verifyOutcome( rowCount, statement, 0 );
                scJdbcCoordinatorRef.getResourceRegistry().release( statement );
                scJdbcCoordinatorRef.afterStatementExecution();
            }
            catch ( SQLException e ) {
                abortBatch();
                String message;
                //Vanilla prepared statement gives no access to parameter values
                if (statement instanceof GPreparedStatement){
                    GPreparedStatement ps = (GPreparedStatement) statement;
                    message = "Could not execute non-batched batch statement for params " + ps.getParams();
                } else {
                    message = "could not execute non-batched batch statement";
                }
                throw sqlExceptionHelper().convert(e, message, entry.getKey());
            }
            catch (JDBCException e) {
                abortBatch();
                throw e;
            }
        }

        getStatements().clear();
    }
}
