package org.narrative.common.util;

/**
 * Any time one or more transactions successfully commit and one or more transactions fail within the same
 * PartitionGroup, we will throw a PartialTransactionCommitException to help identify these potential
 * data integrity problems.
 *
 * @author Brian Lenz
 */
public class PartialTransactionCommitException extends NarrativeException {

    public PartialTransactionCommitException() {}

    public PartialTransactionCommitException(String message) {
        super(message);
    }

    public PartialTransactionCommitException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartialTransactionCommitException(Throwable cause) {
        super(cause);
    }
}