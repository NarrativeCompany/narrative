package org.narrative.common.util.processes;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 12, 2007
 * Time: 11:35:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecentData {
    private final int requests;
    private final long requestTime;
    private final long startTime;
    private final long endTime;

    public RecentData(int requests, long requestTime, long startTime, long endTime) {
        this.requests = requests;
        this.requestTime = requestTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getRequests() {
        return requests;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
