package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/9/18
 * Time: 10:47 AM
 */
public interface DatetimeCountdownProvider {
    OID getOid();

    Date getCountdownTarget();

    Date getCountdownEndingSoonTarget();

    boolean isCountdownTargetLocked();

    String getCountdownRefreshUrl();

    public static final long ENDING_SOON_MS_THRESHOLD = 8 * IPDateUtil.HOUR_IN_MS;

    public static Date getCountdownEndingSoonTarget(DatetimeCountdownProvider provider) {
        return new Date(provider.getCountdownTarget().getTime() - ENDING_SOON_MS_THRESHOLD);
    }

    static long calculateMsToTarget(DatetimeCountdownProvider provider) {
        //  jw: since we do not know how far the clients browser has drifted from reality, let's provide the target as a difference
        //      so that the javascript can apply that difference to the clients own ms value of now. Subtracting 500ms to try and
        //      offset for the known latency between us generating this HTML, and it being sent to the browser, the browser loading
        //      the HTML and interpreting the JS. Unfortunately, all of that is going to change from user to user based one internet
        //      and hardware, but we have no way of really calculating any of that accurately.
        return provider.getCountdownTarget().getTime() - System.currentTimeMillis() - 500;
    }
}
