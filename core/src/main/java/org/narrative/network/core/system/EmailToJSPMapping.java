package org.narrative.network.core.system;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.Task;

import java.util.Properties;

/**
 * User: barry
 * Date: Mar 31, 2009
 * Time: 12:13:51 PM
 */
public enum EmailToJSPMapping {
    INSTANCE;

    private static Properties EMAIL_JSPS;

    void init() {
        EMAIL_JSPS = NetworkRegistry.loadProperties("/emailMappings.properties");
    }

    public String getJSPFileForEmail(final Task emailTask) {
        assert EMAIL_JSPS.containsKey(IPUtil.getClassSimpleName(emailTask.getClass())) : "Could not find email mapping for class! cls/" + IPUtil.getClassSimpleName(emailTask.getClass());
        return EMAIL_JSPS.getProperty(IPUtil.getClassSimpleName(emailTask.getClass()));
    }
}
