package org.narrative.network.core.cluster.setup;

import org.narrative.common.util.Configuration;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.system.NetworkRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: Jan 20, 2006
 * Time: 11:20:18 AM
 *
 * @author Brian
 */
public class NetworkSetup {
    private static final String APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";
    private static final String ACTIVE_SPRING_PROFILES_PROPERTY = "spring.profiles.active";
    private static final String CONTEXT_PROPERTIES_PREFIX = "server.servlet.context-parameters.";

    public static void doSetup() throws IOException {
        doSetup(null, false);
    }

    public static void doSetup(ApplicationContext applicationContext, boolean isInstalling) throws IOException {
        // bl: have to initialize the ApplicationContext on StaticConfig in order to run our CLIs
        if(applicationContext!=null) {
            new StaticConfig().setApplicationContext(applicationContext);
        }

        final Properties applicationProperties = new Properties();
        /* parse the application.properties file to get the list of active profiles defined in spring.profiles.active */
        mergeProperties(parsePropertiesFile(APPLICATION_PROPERTIES_PATH), applicationProperties);

        /* parse the various application-*.properties files in the order defined by the active profiles in spring.profiles.active
         * doing this mimic's what Spring does to allow for the overriding of properties by priority defined in spring.profiles.active */
        final String activeSpringProfiles = applicationProperties.getProperty(ACTIVE_SPRING_PROFILES_PROPERTY);
        if (StringUtils.isEmpty(activeSpringProfiles)) {
            throw UnexpectedError.getRuntimeException("Required property not found: " + ACTIVE_SPRING_PROFILES_PROPERTY);
        }

        for (final String profile : activeSpringProfiles.split(",")) {
            mergeProperties(parsePropertiesFile(String.format("src/main/resources/application-%s.properties", profile)), applicationProperties);
        }

        Configuration.createFileConfiguration(applicationProperties);

        //init the network
        NetworkRegistry.getInstance().init(true, isInstalling);
    }

    public static void doServletSetup() {
        NetworkRegistry.getInstance().init(false, false);
    }

    /* this is done to mimic what occurs in the spring context based initialization of properties via Configuration.createServletConfiguration() */
    protected static String renamePropertyKey(final String propertyKey) {
        if (propertyKey.startsWith(CONTEXT_PROPERTIES_PREFIX)) {
            return propertyKey.substring(CONTEXT_PROPERTIES_PREFIX.length());
        } else {
            return propertyKey;
        }
    }

    /* put the source value at the renamed property key in target */
    protected static void mergeProperties(final Properties source, final Properties target) {
        for (final String prop : source.stringPropertyNames()) {
            target.put(renamePropertyKey(prop), source.getProperty(prop));
        }
    }

    private static Properties parsePropertiesFile(final String propertiesFile) throws IOException {
        final Properties applicationProperties = new Properties();

        try (final InputStream inputStream = new FileInputStream(propertiesFile)) {
            applicationProperties.load(inputStream);
        }

        return applicationProperties;
    }
}