package org.narrative.network.shared.servlet;

import org.narrative.network.shared.struts.NetworkActionMapper;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.ng.ExecuteOperations;
import org.apache.struts2.dispatcher.ng.InitOperations;
import org.apache.struts2.dispatcher.ng.PrepareOperations;
import org.apache.struts2.dispatcher.ng.filter.FilterHostConfig;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * This class exists so we can ultimately create a NetworkDispatcher in GInitOperations.
 * Date: 3/6/15
 * Time: 4:47 PM
 *
 * @author brian
 */
@Component
public class GStrutsPrepareAndExecuteFilter extends StrutsPrepareAndExecuteFilter {
    private final SpringEndpointWhitelist springEndpointWhitelist;

    /**
     * Inject the {@link SpringEndpointWhitelist} so we can ignore API and Actuator requests.
     *
     * @param springEndpointWhitelist the {@link SpringEndpointWhitelist} that will identify requests that bypass Struts
     */
    public GStrutsPrepareAndExecuteFilter(SpringEndpointWhitelist springEndpointWhitelist) {
        this.springEndpointWhitelist = springEndpointWhitelist;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        //If the incoming request is an ignored prefix, skip Struts processing
        if (req instanceof HttpServletRequest) {
            String requestURI = ((HttpServletRequest) req).getRequestURI();
            if(springEndpointWhitelist.isWhitelisted(requestURI)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // if this isn't an ignored prefix, then do the normal struts processing
        super.doFilter(req, res, chain);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        InitOperations init = new GInitOperations();
        Dispatcher dispatcher = null;
        try {
            FilterHostConfig config = new FilterHostConfig(filterConfig);
            init.initLogging(config);
            dispatcher = init.initDispatcher(config);
            init.initStaticContentLoader(config, dispatcher);

            prepare = new PrepareOperations(dispatcher);
            execute = new ExecuteOperations(dispatcher);
            this.excludedPatterns = init.buildExcludedPatternsList(dispatcher);

            postInit(dispatcher, filterConfig);
        } finally {
            if (dispatcher != null) {
                dispatcher.cleanUpAfterInit();
            }
            init.cleanup();
        }

        // bl: now that we have initialized
        NetworkActionMapper.init(dispatcher.getConfigurationManager());
    }
}
