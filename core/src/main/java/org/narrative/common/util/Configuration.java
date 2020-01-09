package org.narrative.common.util;

import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.system.NetworkVersion;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 29, 2005
 * Time: 4:37:20 PM
 */
public class Configuration {

    public static final String SERVLET_CONTEXT_LAST_MODIFIED = "servletContextLastModified";

    private static Configuration configuration;

    private final Properties properties;
    private final ServletContext servletContext;

    private Configuration(ServletContext context) {
        properties = new Properties();
        servletContext = context;
        Enumeration en = context.getInitParameterNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            String val = context.getInitParameter(name);
            properties.setProperty(name, val);
        }

        URL manifestUrl;
        try {
            manifestUrl = context.getResource("META-INF/MANIFEST.MF");
        } catch (MalformedURLException e) {
            throw UnexpectedError.getRuntimeException("Failed to identify MANIFEST.MF file!", e);
        }

        Manifest mf = new Manifest();
        try(InputStream manifestFile = manifestUrl.openStream()) {
            mf.read(manifestFile);
        } catch (IOException ioex){
            throw UnexpectedError.getRuntimeException("Failed loading manifest file! Unable to identify version!", ioex);
        }

        Attributes atts = mf.getMainAttributes();

        initNetworkVersion(atts);
    }

    public Configuration(Properties properties) {
        this.properties = properties;
        this.servletContext = null;
        initNetworkVersion(null);
    }

    private void initNetworkVersion(Attributes atts) {
        String version;
        long build;
        String branch;
        String gitSha;
        if(atts==null || StaticConfig.getBean(NarrativeProperties.class).getCluster().getEnvironmentType().isLocal()) {
            // bl: for local servers, include the current time in the version on each restart so each startup acts as a new version
            build = System.currentTimeMillis();
            branch = "dev";
            gitSha = "local00";
            version = "0.0.1-LOCAL-" + build;
        } else {
            version = atts.getValue("Implementation-Version");
            build = Long.parseLong(atts.getValue("Implementation-JenkinsBuild"));
            branch = atts.getValue("Implementation-Branch");
            // this is the full 40-character SHA-1 git commit hash
            gitSha = atts.getValue("Implementation-Revision");
            // this date is an epoch value in seconds, so multiply by 1000
            long date = Long.parseLong(atts.getValue("Implementation-Date")) * IPDateUtil.SECOND_IN_MS;
            properties.setProperty(SERVLET_CONTEXT_LAST_MODIFIED, Long.toString(date));
        }

        NetworkVersion.INSTANCE.init(version, build, branch, gitSha);
    }

    public static Configuration getConfiguration() {
        assert configuration != null : "Configuration must be created before calling it";
        return configuration;
    }

    public static void createServletConfiguration(ServletContext context) {
        configuration = new Configuration(context);
    }

    public static void createFileConfiguration(Properties properties) {
        configuration = new Configuration(properties);
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw UnexpectedError.getRuntimeException("Required property not found: " + name);
        }

        return value;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
        if (servletContext != null) {
            servletContext.setAttribute(name, value);
        }
    }

    public void setObject(String name, Object value) {
        if (servletContext != null) {
            servletContext.setAttribute(name, value);
        }
    }

    public File getServletContextRootPath() {
        return new File(servletContext.getRealPath("/"));
    }

    public URL getResource(String path) throws MalformedURLException {
        return servletContext.getResource(path);
    }
}
