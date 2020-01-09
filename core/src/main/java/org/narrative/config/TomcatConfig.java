package org.narrative.config;

import org.narrative.common.util.UnexpectedError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.shared.servlet.GStrutsPrepareAndExecuteFilter;
import org.narrative.network.shared.servlet.NetworkFilter;
import org.narrative.network.shared.servlet.NetworkResponseWrapperFilter;
import org.narrative.network.shared.servlet.NetworkStaticWebInfFilter;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.tomcat.util.descriptor.web.ServletDef;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Date: 9/22/18
 * Time: 11:50 AM
 *
 * @author brian
 */
@Configuration
public class TomcatConfig {
    private static final NetworkLogger logger = new NetworkLogger(TomcatConfig.class);

    private static final String DISPATCHER_SERVLET = "dispatcherServlet";

    @Bean
	public ServletWebServerFactory servletContainer(NarrativeProperties narrativeProperties) {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector(narrativeProperties.getCluster().getPort()));
		tomcat.addAdditionalTomcatConnectors(createStandardConnector(narrativeProperties.getKycQueue().getPort()));
		return tomcat;
	}

	private Connector createStandardConnector(int port) {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		// bl: initialize the additional connector on the cluster CP port
		connector.setPort(port);
		return connector;
	}

    /**
     * Registering NetworkResponseWrapper and NetworkFilter filters (not dispatcher servlets) before Spring Security
     * filters. Works with {spring.security.filter.order} property.
     */
    @Bean
    public FilterRegistrationBean registerNetworkResponseFilter(NetworkResponseWrapperFilter filter) {
        FilterRegistrationBean reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(1);
        return reg;
    }
    @Bean
    public FilterRegistrationBean registerNetworkFilter(NetworkFilter filter) {
        FilterRegistrationBean reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(2);
        return reg;
    }

    @Bean
    public FilterRegistrationBean gStrutsPrepareAndExecuteFilterRegistrationBean(GStrutsPrepareAndExecuteFilter filterBean) {
        FilterRegistrationBean <GStrutsPrepareAndExecuteFilter> filterRegBean = new FilterRegistrationBean<>(filterBean);
        filterRegBean.setServletNames(Collections.singletonList(DISPATCHER_SERVLET));
        filterRegBean.setOrder(30);
        return filterRegBean;
    }

    @Bean
    public ServletRegistrationBean defaultStaticServletRegistrationBean() {
        ServletRegistrationBean<DefaultServlet> defaultStaticServletServletRegistrationBean = new ServletRegistrationBean<>(new DefaultServlet());
        defaultStaticServletServletRegistrationBean.addUrlMappings("/static-legacy/*");
        return defaultStaticServletServletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean networkStaticWebInfFilterRegistrationBean(NetworkStaticWebInfFilter filterBean, ServletRegistrationBean defaultStaticServletRegistrationBean) {
        return new FilterRegistrationBean<>(filterBean, defaultStaticServletRegistrationBean);
    }

    @Bean
    public ServletContextInitializer registerPreCompiledJsps(NarrativeProperties narrativeProperties) {
        // pull the web.xml config with pre-compiled JSPs and inject them into embedded Tomcat's ServletContext.
        // pulled from: https://stackoverflow.com/a/35651338/4231143
        return servletContext -> {
            boolean isRequirePreCompiledJsps = !narrativeProperties.getCluster().getEnvironmentType().isLocal();
            URL precompiledJspWebXml;
            try {
                precompiledJspWebXml = servletContext.getResource(narrativeProperties.getTomcat().getWebXml());
            } catch (MalformedURLException e) {
                throw UnexpectedError.getRuntimeException("Invalid web.xml path found!", e);
            }
            if(precompiledJspWebXml==null) {
                if(!isRequirePreCompiledJsps) {
                    if(logger.isInfoEnabled()) logger.info("Skipping registration of pre-compiled JSP servlets, as web.xml not found!");
                    return;
                }
                // bl: on all other environments, we should be running in a WAR with embedded tomcat and always
                // should have pre-compiled JSPs defined in the web.xml!
                throw UnexpectedError.getRuntimeException("Failed to identify " + narrativeProperties.getTomcat().getWebXml() + ", so pre-compiled JSPs could not be loaded!");
            }

            // Use Tomcat's web.xml parser (assume complete XML file and validate).
            // bl: disabling validation since it won't load otherwise. gives an error:
            //     org.xml.sax.SAXParseException: cvc-elt.1: Cannot find the declaration of element 'web-app'.
            WebXmlParser parser = new WebXmlParser(false, false, true);
            try (InputStream is = precompiledJspWebXml.openStream()) {
                WebXml webXml = new WebXml();
                boolean success = parser.parseWebXml(new InputSource(is), webXml, false);
                if (!success) {
                    throw new RuntimeException("Error parsing web.xml " + precompiledJspWebXml + ". The parse error should be displayed in the logs above.");
                }

                if(isRequirePreCompiledJsps && webXml.getServlets().isEmpty()) {
                    throw UnexpectedError.getRuntimeException("Failed to identify any pre-compiled JSP servlets! This indicates an improperly built WAR file.");
                }

                // bl: the only servlets that should be defined in the web.xml are
                for (ServletDef def : webXml.getServlets().values()) {
                    if(logger.isDebugEnabled()) logger.debug("Registering pre-compiled JSP: " + def.getServletName() + " -> " + def.getServletClass());
                    servletContext.addServlet(def.getServletName(), def.getServletClass());
                }

                boolean hasMappings = !webXml.getServletMappings().isEmpty();

                if(isRequirePreCompiledJsps && !hasMappings) {
                    throw UnexpectedError.getRuntimeException("Found pre-compiled JSP servlets, but failed to identify any servlet mappings! This indicates an improperly built WAR file.");
                } else if(!isRequirePreCompiledJsps && hasMappings) {
                    if(logger.isWarnEnabled()) logger.warn("Running with pre-compiled JSPs on development environment!");
                }

                for (Map.Entry<String, String> mapping : webXml.getServletMappings().entrySet()) {
                    if(logger.isDebugEnabled()) logger.debug("Mapping pre-compiled JSP servlet: " + mapping.getValue() + " -> " + mapping.getKey());
                    servletContext.getServletRegistration(mapping.getValue()).addMapping(mapping.getKey());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error registering pre-compiled JSPs", e);
            }
        };
    }
}
