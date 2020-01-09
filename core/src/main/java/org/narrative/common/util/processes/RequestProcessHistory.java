package org.narrative.common.util.processes;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.LRUMap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 19, 2005
 * Time: 10:53:03 AM
 */
public class RequestProcessHistory extends GenericHistory {
    // bl: pound times out after 30 seconds, so consider anything over 25 seconds an "outlier" for now
    private static final long LONG_REQUEST_MS = 25 * IPDateUtil.SECOND_IN_MS;
    private static final LRUMap<String, Queue<SlowRequestDetails>> MOST_COMMON_SLOW_REQUESTS = new LRUMap<String, Queue<SlowRequestDetails>>(100);

    public RequestProcessHistory(RequestProcessBase r) {
        super(r);
    }

    @Override
    public long getMinOutlierRequestTimeMs() {
        return LONG_REQUEST_MS;
    }

    @Override
    protected void addRequest(GenericProcess process, double processRunningTime, double actualRunningTime) {
        super.addRequest(process, processRunningTime, actualRunningTime);
        if (isOutlier(processRunningTime)) {
            Queue<SlowRequestDetails> requestDetails = MOST_COMMON_SLOW_REQUESTS.get(process.getName());
            if (requestDetails == null) {
                MOST_COMMON_SLOW_REQUESTS.put(process.getName(), requestDetails = new ConcurrentLinkedQueue<SlowRequestDetails>());
            }
            StringBuilder extraLogInfo = new StringBuilder(Thread.currentThread().getName());
            extraLogInfo.append(" ");
            extraLogInfo.append(new Timestamp(System.currentTimeMillis()).toString());
            extraLogInfo.append(" ");
            // todo: fix this workaround. should create some notion of a NarrativeContext
            // as the base for NetworkContext and put the RequestResponseHandler (reqResp) as a property on NarrativeContext.
            // that way, we can get the current NarrativeContext and get the log information from it.
            // had to create this workaround on NarrativeLogger in order to sever an old dependency on
            // network code here.
            extraLogInfo.append(NarrativeLogger.getDefaultCurrentContextInfo());
            requestDetails.add(new SlowRequestDetails(processRunningTime, extraLogInfo.toString()));
            while (requestDetails.size() > 5) {
                // bl: just do a poll, not a remove so that we won't get an exception in case there aren't any more items
                // in the queue due to multiple threads accessing this code at the same time.
                requestDetails.poll();
            }
        }
    }

    public static List<SlowRequestDetails> getRecentSlowActionInfo(String processName) {
        Queue<SlowRequestDetails> infos = MOST_COMMON_SLOW_REQUESTS.get(processName);
        return infos == null ? Collections.EMPTY_LIST : new ArrayList<SlowRequestDetails>(infos);
    }

    public static class SlowRequestDetails extends ObjectPair<Double, String> {
        private SlowRequestDetails(Double processRunningTime, String logInfo) {
            super(processRunningTime, logInfo);
        }

        public double getProcessRunningTime() {
            return getOne();
        }

        public String getLogInfo() {
            return getTwo();
        }
    }
}
