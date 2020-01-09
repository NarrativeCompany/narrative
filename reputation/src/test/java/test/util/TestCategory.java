package test.util;

import lombok.Getter;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper to determine if specific test categories are currently enabled
 */
@Getter
public class TestCategory {
    private static final Set<String> enabledCategories;
    private static final boolean integTestsDisabled;
    private static final boolean integContainerizedTestsDisabled;

    static {
        String categories = System.getProperty("enabledTestCategories");
        if (StringUtils.isNotEmpty(categories)) {
            String[] catArr = categories.split(",");
            enabledCategories = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(catArr)));
        } else {
            enabledCategories = Collections.emptySet();
        }
        integTestsDisabled = !enabledCategories.contains("integ");
        integContainerizedTestsDisabled = !enabledCategories.contains("integContainerized");
    }

    public static Set<String> getEnabledCategories() {
        return enabledCategories;
    }

    public static boolean isIntegTestsDisabled() {
        return integTestsDisabled;
    }

    public static boolean isIntegContainerizedTestsDisabled() {
        return integContainerizedTestsDisabled;
    }
}
