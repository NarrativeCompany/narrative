package org.narrative.network.core.statistics;

import org.narrative.common.util.Debug;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.posting.HtmlTextMassager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: Mar 28, 2007
 * Time: 9:09:00 AM
 *
 * @author Brian
 */
public class ExceptionInfo implements ErrorInfo<String> {
    private final Throwable exception;
    private final int stackTraceHashcode;
    private final int rootCauseHashcode;
    private final Queue<String> extraLogInfos = new ConcurrentLinkedQueue<String>();
    private int count = 1;
    private int spiderCount = 0;
    private int bubbledToFilterCount = 0;

    public ExceptionInfo(Throwable exception) {
        this.exception = exception;
        this.stackTraceHashcode = Debug.getHashCodeFromException(exception);
        this.rootCauseHashcode = Debug.getRootCauseClassHashCode(exception);
    }

    public void addCount() {
        count++;
    }

    public void addSpiderCount() {
        spiderCount++;
    }

    public void addBubbledToFilterCount() {
        bubbledToFilterCount++;
    }

    public Throwable getException() {
        return exception;
    }

    /**
     * bl: getting the message off of the exception directly via JSP causes problems in some cases (e.g.
     * HibernateExceptions).  my suspicion is that JSP doesn't like the fact that HibernateExceptions (NestableRuntimeExceptions, specifically)
     * have a "public String getMessage(int index)" function.  perhaps the JSP engine sees that method in addition
     * to the standard "public String getMessage()" method and chokes for some reason.  it really shouldn't have a problem
     * at all, but it does for whatever reason.  nevertheless, calling exception.getMessage() explicitly in the java
     * code solves the problem.
     *
     * @return the message from the exception.
     */
    public String getExceptionMessage() {
        return exception.getMessage();
    }

    @Override
    public String getErrorMessage() {
        return getRootCauseExceptionMessage();
    }

    public String getRootCauseExceptionMessage() {
        return getRootCauseException().getMessage();
    }

    @Override
    public int getStackTraceHashcode() {
        return stackTraceHashcode;
    }

    public int getRootCauseHashcode() {
        return rootCauseHashcode;
    }

    public Throwable getRootCauseException() {
        return Debug.getRootCause(exception);
    }

    public Class getRootCauseExceptionClass() {
        return getRootCauseException().getClass();
    }

    public void addExtraLogInfo(String extraLogInfo) {
        if (IPStringUtil.isEmpty(extraLogInfo)) {
            return;
        }
        extraLogInfos.add(extraLogInfo);
        while (extraLogInfos.size() > 5) {
            // bl: just do a poll, not a remove so that we won't get an exception in case there aren't any more items
            // in the queue due to multiple threads accessing this code at the same time.
            extraLogInfos.poll();
        }
    }

    @Override
    public List<String> getRequestInfos() {
        return new ArrayList<String>(extraLogInfos);
    }

    @Override
    public String getStackTrace() {
        return HtmlTextMassager.stackTraceAsHtml(exception);
    }

    @Override
    public int getCount() {
        return count;
    }

    public int getSpiderCount() {
        return spiderCount;
    }

    public int getBubbledToFilterCount() {
        return bubbledToFilterCount;
    }
}
