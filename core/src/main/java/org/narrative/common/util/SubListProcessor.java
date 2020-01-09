package org.narrative.common.util;

import java.util.List;

/**
 * Date: 7/14/13
 * Time: 7:53 AM
 * User: jonmark
 * <p>
 * The idea with this interface is to streamline the common problem of operating on sub lists of data.  We are almost
 * always just getting a sublist iterator and then while it has next, process next, this should allow us to centralize
 * that iterable logic into CoreUtils and not have to worry about duplicating that code everywhere.
 */
public interface SubListProcessor<T> {
    public void processSubList(List<T> subList);

}
