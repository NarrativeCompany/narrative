package org.narrative.common.util.processes;

/**
 * Date: Apr 29, 2008
 * Time: 7:58:40 AM
 *
 * @author brian
 */
public class StatHolder {
    public long createTime = System.currentTimeMillis();
    public int totalTime;
    public int totalRequests;

    public void addRequest(GenericProcess process) {
        if (process instanceof RequestProcessBase) {
            totalTime += process.getTotalRunningTime();
            totalRequests++;
        }
    }

    public int getRequestsPerMinute() {
        if (totalRequests == 0) {
            return 0;
        }
        double minutes = (double) (System.currentTimeMillis() - createTime) / (double) totalRequests;
        return (int) (minutes / (double) totalRequests);
    }

    public int getResponseTime() {
        if (totalRequests == 0) {
            return 0;
        }
        return (int) ((double) totalTime / (double) totalRequests);
    }
}
