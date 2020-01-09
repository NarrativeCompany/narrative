package org.narrative.common.persistence;

/**
 * Date: Apr 30, 2008
 * Time: 2:30:19 PM
 *
 * @author Steven Benitez
 */
public interface PartitionGroupOptions {
    public void addEndOfPartitionGroupRunnableForSuccessOrError(Runnable runnable);
}
