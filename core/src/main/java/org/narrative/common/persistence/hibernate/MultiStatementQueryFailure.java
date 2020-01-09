package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.NarrativeException;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 7, 2007
 * Time: 3:23:00 PM
 * Used for communicating to a client that the multi statement didn't run correctly, and failed at the specified query position
 */
public class MultiStatementQueryFailure extends NarrativeException {
    private int failedOnQueryNumber;

    public MultiStatementQueryFailure(String message, int failedOnQueryNumber, Exception e) {
        super(message, e);
        this.failedOnQueryNumber = failedOnQueryNumber;
    }

    public int getFailedOnQueryNumber() {
        return failedOnQueryNumber;
    }
}
