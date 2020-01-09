package org.narrative.common.util;

/**
 * Date: 2019-07-03
 * Time: 15:20
 *
 * @author brian
 */
public class JUnitUtil {
    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
