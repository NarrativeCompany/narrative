package org.narrative.common.persistence.hibernate;

import org.hibernate.Transaction;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 1, 2007
 * Time: 9:23:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class GTransaction {
    private static final Object tranLock = new Object();
    private static long nextTransactionId = 0;

    private final Transaction transaction;
    private final long id;

    public GTransaction(Transaction transaction) {
        this.transaction = transaction;
        synchronized (tranLock) {
            id = nextTransactionId++;
        }
        transaction.begin();
    }

    public long getId() {
        return id;
    }

    public boolean isActive() {
        return transaction.isActive();
    }

    /**
     * commit the transaction.
     * NOTE: package-level access so that transactions can only be committed through GSession
     */
    void commit() {
        transaction.commit();
    }

    /**
     * rollback the transaction.
     * NOTE: package-level access so that transactions can only be rolled back through GSession
     */
    void rollback() {
        transaction.rollback();
    }
}
