package org.narrative.common.util.processes;

import org.narrative.common.util.IPDateUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 19, 2005
 * Time: 10:45:22 AM
 */
public class HistoryManager {

    static private HistoryManager instance;
    private final Map<String, Map<String, GenericHistory>> historyMap = new ConcurrentHashMap<String, Map<String, GenericHistory>>();

    private long nextStatRecord = System.currentTimeMillis() + IPDateUtil.MINUTE_IN_MS * 10;
    private final Object statRecordLock = new Object();
    private StatHolder statHolder = new StatHolder();
    private StatisticRecorder statisticRecorder;

    //recent history
    private long lastTime = 0;
    private int recentNumberOfRequests = 0;
    private long recentRequestTimeMS;
    private final Object lock = new Object();
    private RecentData recentData = null;

    private static final Comparator<GenericHistory> GENERIC_HISTORY_COMPARATOR = new Comparator<GenericHistory>() {
        public int compare(GenericHistory o1, GenericHistory o2) {
            int ret = Long.valueOf(o2.getAllRequestInfo().getOwnRequestRunningTimeMS()).compareTo(o1.getAllRequestInfo().getOwnRequestRunningTimeMS());
            if (ret != 0) {
                return ret;
            }
            ret = Integer.valueOf(o2.getAllRequestInfo().getNumberOfRequests()).compareTo(o1.getAllRequestInfo().getNumberOfRequests());
            if (ret != 0) {
                return ret;
            }
            ret = o1.getName().compareToIgnoreCase(o2.getName());
            if (ret != 0) {
                return ret;
            }
            return o1.getProcessType().compareToIgnoreCase(o2.getProcessType());
        }
    };

    public static void init(StatisticRecorder statisticRecorder) {
        HistoryManager historyManager = getInstance();
        historyManager.statisticRecorder = statisticRecorder;
    }

    private HistoryManager() {}

    public static HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager();
        }
        return instance;
    }

    public Collection<GenericHistory> getHistoryForType(String type) {
        Map<String, GenericHistory> map = historyMap.get(type);
        if (map != null) {
            return map.values();
        } else {
            return Collections.emptySet();
        }
    }

    public Set<GenericHistory> getAllHistory() {
        Set<GenericHistory> ret = new TreeSet<GenericHistory>(GENERIC_HISTORY_COMPARATOR);

        for (Map<String, GenericHistory> map : historyMap.values()) {
            ret.addAll(map.values());
        }

        return ret;
    }

    public void recordProcess(GenericProcess process) {
        Map<String, GenericHistory> map = historyMap.get(process.getType());
        if (map == null) {
            synchronized (historyMap) {
                map = historyMap.get(process.getType());
                if (map == null) {
                    map = new ConcurrentHashMap<String, GenericHistory>();
                    historyMap.put(process.getType(), map);
                }
            }
        }

        GenericHistory history = map.get(process.getName());
        if (history == null) {
            synchronized (map) {
                history = map.get(process.getName());
                if (history == null) {
                    history = process.getHistory();
                    map.put(process.getName(), history);
                }
            }
        }

        history.addRequest(process);

        addRecentHistory(process, history);

        if (statisticRecorder != null) {
            //do any stat records required
            if (System.currentTimeMillis() > nextStatRecord) {
                synchronized (statRecordLock) {
                    if (System.currentTimeMillis() > nextStatRecord) {
                        nextStatRecord = System.currentTimeMillis() + IPDateUtil.MINUTE_IN_MS * 10;
                        StatHolder oldStatHolder = statHolder;  //pm: I know there's a potential to loose a request between this line and the next but its not worth the sync on every request
                        statHolder = new StatHolder();
                        statisticRecorder.recordStats(oldStatHolder);
                    }
                }
            }

            if (process instanceof RequestProcessBase) {
                statHolder.addRequest(process);
            }
        }
    }

    private void addRecentHistory(GenericProcess process, GenericHistory history) {

        //only record response times of actions
        if (history instanceof RequestProcessHistory) {
            long curTime = System.currentTimeMillis();
            if (lastTime + IPDateUtil.SECOND_IN_MS * 30 < curTime) {
                synchronized (lock) {
                    if (lastTime + IPDateUtil.SECOND_IN_MS * 30 < curTime) {
                        recentNumberOfRequests++;
                        recentRequestTimeMS += process.getTotalRunningTime();
                        recentData = new RecentData(recentNumberOfRequests, recentRequestTimeMS, lastTime + 1, curTime);
                        recentNumberOfRequests = 0;
                        recentRequestTimeMS = 0;

                        //make sure lastTime is updated at the end, as this impacts who gets blocked
                        lastTime = curTime;
                        return;
                    }
                }
            }

            //always record after the sync block to insure blocked threads don't record while we are in the sync block
            recentNumberOfRequests++;
            recentRequestTimeMS += process.getTotalRunningTime();

        }
    }

    public RecentData getRecentData() {
        return recentData;
    }

}
