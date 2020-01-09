package org.narrative.network.customizations.narrative.security;

import org.narrative.common.util.ClientAgentInformation;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.shared.security.PrimaryRole;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Filter to initialize data visibility context for a user on a per-request basis.
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataVisibilityFilter extends OncePerRequestFilter {
    private final StaticMethodWrapper staticMethodWrapper;

    public DataVisibilityFilter(StaticMethodWrapper staticMethodWrapper) {
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        ClientAgentInformation agentInformation = staticMethodWrapper.networkContext().getReqResp().getClientAgentInformation();

        if (agentInformation == null) {
            throw UnexpectedError.getRuntimeException("Agent information is null for request!");
        }

        PrimaryRole primaryRole = staticMethodWrapper.networkContext().getPrimaryRole();
        Set<AgeRating> preferredAgeRatingSet;
        Set<AgeRating> permittedAgeRatingSet;

        boolean isRegisteredUser = primaryRole.isRegisteredUser();

        // Pre-render has no restrictions
        if (agentInformation.getClientAgentType().isPrerender()) {
            preferredAgeRatingSet = AgeRating.ALL_AGE_RATINGS;
            permittedAgeRatingSet = AgeRating.ALL_AGE_RATINGS;
        } else {
            // Use the user's preferred age ratings rather than all age ratings available to the user
            preferredAgeRatingSet = primaryRole.getPreferredAgeRatings();
            // Also keep track of all of the user's allowed age ratings
            permittedAgeRatingSet = primaryRole.getPermittedAgeRatings();
        }

        QualityFilter contentQualityFilter = isRegisteredUser ?
                primaryRole.getUser().getPreferences().getContentQualityFilter() :
                QualityFilter.DEFAULT_FILTER;

        DataVisibilityContextManager.setDataVisibilityContext(new DataVisibilityContext(preferredAgeRatingSet, permittedAgeRatingSet, contentQualityFilter));

        log.debug("Applying DataVisibilityFilter with values {}", preferredAgeRatingSet);

        //Continue processing the filter chain
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear the thread local after the request completes
            DataVisibilityContextManager.clearDataVisibilityContext();
        }
    }
}
