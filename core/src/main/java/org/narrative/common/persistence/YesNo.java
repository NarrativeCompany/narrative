package org.narrative.common.persistence;

import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 14, 2004
 * Time: 1:49:17 PM
 */
public class YesNo {

    private static final NarrativeLogger logger = new NarrativeLogger(YesNo.class);

    private static final String YES_STR = "Y";
    private static final String NO_STR = "N";

    private static final YesNo YES = new YesNo(true);
    private static final YesNo NO = new YesNo(false);

    private boolean value;

    private YesNo(boolean yORn) {
        value = yORn;
    }

    public static YesNo valueOf(String yORn) {
        Debug.assertMsg(logger, yORn == null || yORn.equalsIgnoreCase(YES_STR) || yORn.equalsIgnoreCase(NO_STR), "yORn must not be null and must be a 'Y' or an 'N'");
        return valueOf(YES_STR.equalsIgnoreCase(yORn));
    }

    public static YesNo valueOf(boolean yORn) {
        if (yORn) {
            return YES;
        }
        return NO;
    }

    @SuppressWarnings("squid:S2259")
    public static YesNo valueOf(Boolean yORn) {
        if (yORn == null) {
            throw UnexpectedError.getRuntimeException("yORn Boolean object must be non-null!");
        }
        return valueOf(yORn.booleanValue()); // squid:S2259 Null pointer is not dereferenced here. Verified by unit test.
    }

    public String toString() {
        if (value) {
            return YES_STR;
        } else {
            return NO_STR;
        }
    }
}
