package org.narrative.network.core.statistics;

import java.util.List;

/**
 * Date: 8/17/15
 * Time: 12:51 PM
 *
 * @author brian
 */
public interface ErrorInfo<T> {
    public String getErrorMessage();

    public int getStackTraceHashcode();

    public String getStackTrace();

    public int getCount();

    public List<T> getRequestInfos();
}
