package org.narrative.common.util.processes;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 30, 2005
 * Time: 11:34:28 AM
 */
public class GenericHistory {
    private final RequestInfo allRequestInfo = new RequestInfo();
    private final RequestInfo outlierRequestInfo = new RequestInfo();
    private String name;
    private String processType;
    private boolean isForRootProcess = false;

    /*private RecentHistory recentHistory = new RecentHistory(System.currentTimeMillis());
    private static final Object recentHistoryLock = new Object();
    private static final Object oldRecentHistoryLock = new Object();*/

    public GenericHistory(GenericProcess p) {
        this.name = p.getName();
        this.processType = p.getType();
    }

    public RequestInfo getAllRequestInfo() {
        return allRequestInfo;
    }

    public RequestInfo getOutlierRequestInfo() {
        return outlierRequestInfo;
    }

    public long getMinOutlierRequestTimeMs() {
        return Long.MAX_VALUE;
    }

    protected final boolean isOutlier(double requestMs) {
        return requestMs >= getMinOutlierRequestTimeMs();
    }

    public void addRequest(GenericProcess process) {
        addRequest(process, process.getTotalRunningTime(), process.getOwnRunningTime());
    }

    protected void addRequest(GenericProcess process, double processRunningTime, double actualRunningTime) {
        allRequestInfo.recordRequest(processRunningTime, actualRunningTime);
        if (isOutlier(processRunningTime)) {
            outlierRequestInfo.recordRequest(processRunningTime, actualRunningTime);
        }
        // bl: always allow "upgrade" to root in case this process is ever used as a root
        // but is also sometimes used as a non-root process.
        if (process.isRootProcess()) {
            isForRootProcess = true;
        }
//todo:get this working at some point
//        //add to recent history
//        long time = System.currentTimeMillis();
//        RecentHistory localRH = recentHistory;
//        if (time - localRH.getHistoryCreationDatetime() > IPDateUtil.HOUR_IN_MS) {
//            RecentHistory oldLocalRH = null;
//            synchronized(recentHistoryLock) {
//                if (time - recentHistory.getHistoryCreationDatetime() > IPDateUtil.HOUR_IN_MS) {
//                    oldLocalRH = recentHistory;
//                    recentHistory = new RecentHistory(recentHistory.getHistoryCreationDatetime()+IPDateUtil.HOUR_IN_MS);
//                    localRH = recentHistory;
//                }
//            }
//            if (oldLocalRH != null) {
//                synchronized(oldRecentHistoryLock) {
//                    //todo: build history maps
//                }
//            }
//        }
//        localRH.addRequest(process,  time);
    }

    public String getName() {
        return name;
    }

    public String getProcessType() {
        return processType;
    }

    public boolean isForRootProcess() {
        return isForRootProcess;
    }

    public static class RequestInfo {
        private int numberOfRequests = 0;
        private long totalRequestTimeMS;
        private long ownRequestRunningTimeMS;

        private void recordRequest(double processRunningTime, double actualRunningTime) {
            totalRequestTimeMS += processRunningTime;
            ownRequestRunningTimeMS += actualRunningTime;
            numberOfRequests++;
        }

        public int getNumberOfRequests() {
            return numberOfRequests;
        }

        public long getTotalRequestTimeMS() {
            return totalRequestTimeMS;
        }

        public long getOwnRequestRunningTimeMS() {
            return ownRequestRunningTimeMS;
        }

        public String getAverageTotalMSPerRequest() {
            if (numberOfRequests > 0) {
                return new DecimalFormat("##########.00").format((double) totalRequestTimeMS / (double) numberOfRequests);
            }
            return "0.00";
        }

        public String getAverageOwnMSPerRequest() {
            if (numberOfRequests > 0) {
                return new DecimalFormat("##########.00").format((double) ownRequestRunningTimeMS / (double) numberOfRequests);
            }
            return "0.00";
        }
    }
}
