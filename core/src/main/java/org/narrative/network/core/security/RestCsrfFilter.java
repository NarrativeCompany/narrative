package org.narrative.network.core.security;

import org.narrative.common.util.ApplicationError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Filter that will:
 * Check Origin header and make sure it is set to our origin
 * Check X-Requested-With header and make sure it is XMLHttpRequest
 */
public class RestCsrfFilter extends OncePerRequestFilter {
    private final RequestMatcher postRequestMatcher;
    private final RequestMatcher putRequestMatcher;
    private final RequestMatcher patchRequestMatcher;
    private final RequestMatcher deleteRequestMatcher;
    private final HandlerExceptionResolver resolver;
    private final String originUri;
    private final Pattern corsBypassPattern;
    private static final NetworkLogger logger = new NetworkLogger(RestCsrfFilter.class);

    public RestCsrfFilter(NarrativeProperties narrativeProperties, HandlerExceptionResolver resolver) {
        this.resolver = resolver;
        String apiUri = narrativeProperties.getSpring().getMvc().getBaseUri();

        this.postRequestMatcher = new AntPathRequestMatcher(apiUri + "**", HttpMethod.POST.name());
        this.putRequestMatcher = new AntPathRequestMatcher(apiUri + "**", HttpMethod.PUT.name());
        this.patchRequestMatcher = new AntPathRequestMatcher(apiUri + "**", HttpMethod.PATCH.name());
        this.deleteRequestMatcher = new AntPathRequestMatcher(apiUri + "**", HttpMethod.DELETE.name());
        this.originUri = narrativeProperties.getCluster().getPlatformUrl();
        this.corsBypassPattern = Pattern.compile(apiUri + narrativeProperties.getSpring().getMvc().getCorsBypassPattern());
    }

    /**
     * Can be overridden in subclasses for custom filtering control,
     * returning {@code true} to avoid filtering of the given request.
     * <p>The default implementation always returns {@code false}.
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only filter requests to /api endpoints that cause mutations
        return !(postRequestMatcher.matches(request) || putRequestMatcher.matches(request) || patchRequestMatcher.matches(request) || deleteRequestMatcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String originHeader = request.getHeader("Origin");

            if (originHeader == null || !originHeader.equals(originUri)) {
                if(!isAllowBypassCorsCheck(originHeader, request.getRequestURI())) {
                    throw new ApplicationError(wordlet("rest.csrfError"));
                }
            }

        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);
            return;
        }

        filterChain.doFilter(request, response);

    }

    /**
     * we need to bypass the CORS Origin header check for requests that don't use the fetch API.
     * this is because firefox brilliantly doesn't pass the Origin header for XMLHttpRequests by default.
     *
     * note that we will only allow requests if the Origin is empty. if the Origin is specified, that's a CORS
     * request that we need to reject!
     *
     * we will also only allow the request for authenticated users, not guests. guests will get the error.
     * @param originHeader the Origin header
     * @param requestUri the current request URI
     * @return true if the request allows a bypass of the CORS Origin header check
     */
    private boolean isAllowBypassCorsCheck(String originHeader, String requestUri) {
        if(StringUtils.isNotEmpty(originHeader)) {
            return false;
        }
        if(!NetworkContextImplBase.current().getPrimaryRole().isRegisteredUser()) {
            return false;
        }
        return corsBypassPattern.matcher(requestUri).matches();
    }
}
