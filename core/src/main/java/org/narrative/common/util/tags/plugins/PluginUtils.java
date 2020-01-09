package org.narrative.common.util.tags.plugins;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 8, 2006
 * Time: 12:01:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginUtils {
    private static final Map<String, String> FQCN_TO_BINARY_CLASS_NAME = new ConcurrentHashMap<String, String>();

    public static String getBinaryClassNameFromFullyQualifiedClassName(String className) {
        String ret = FQCN_TO_BINARY_CLASS_NAME.get(className);
        if (ret != null) {
            return ret;
        }

        StringBuilder sb = new StringBuilder();
        boolean foundClass = false;
        StringTokenizer st = new StringTokenizer(className, ".");
        while (st.hasMoreTokens()) {
            String packageOrClassName = st.nextToken();
            if (packageOrClassName == null || packageOrClassName.length() == 0) {
                continue;
            }
            sb.append(packageOrClassName);
            // as soon as we find a package/class name that starts with an upper case letter, that means
            // we found the class, so start separating with $.
            if (Character.isUpperCase(packageOrClassName.charAt(0))) {
                foundClass = true;
            }
            if (st.hasMoreTokens()) {
                // separate with dots once we found the class
                if (foundClass) {
                    sb.append("$");
                } else {
                    sb.append(".");
                }
            }
        }

        ret = sb.toString();
        FQCN_TO_BINARY_CLASS_NAME.put(className, ret);
        return ret;
    }

}
