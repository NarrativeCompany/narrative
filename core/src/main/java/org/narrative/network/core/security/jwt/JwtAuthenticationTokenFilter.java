package org.narrative.network.core.security.jwt;

import org.narrative.network.core.security.NarrativeAuthenticationToken;
import org.narrative.network.core.security.NarrativeUserDetailsService;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.security.Guest;
import org.narrative.network.shared.util.NetworkLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final NetworkLogger logger = new NetworkLogger(JwtAuthenticationTokenFilter.class);

    private final JwtUtil jwtUtil;
    private final NarrativeUserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationTokenFilter(JwtUtil jwtUtil, NarrativeUserDetailsService userDetailsService, HandlerExceptionResolver resolver) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        String jwtString = jwtUtil.extractJwtStringFromRequest(request);

        if (StringUtils.isEmpty(jwtString)) {
            AuthZone authZone = NetworkContextImplBase.current().getAuthZone();
            Guest guest = new Guest(authZone);

            // Authenticate guest user on security context.
            UserDetails guestDetails = userDetailsService.buildGuestUser(guest);
            UsernamePasswordAuthenticationToken guestAuthentication = new UsernamePasswordAuthenticationToken(guestDetails, null, guestDetails.getAuthorities());
            guestAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(guestAuthentication);

            // Set guest User as primary role on NetworkContext.
            NetworkContextImplBase.current().setPrimaryRole(guest);
        }
        else {
            try {
                //Parse and validate the JWT
                Jws<Claims> jws = jwtUtil.parseAndValidateJWTFromString(jwtString);

                //Resolve the user and auth token from the JWT - always validate 2FA expiry in the token here since we
                //should never get here with an expired JWT 2FA expiry unless it is truly expired
                Pair<User, NarrativeAuthenticationToken> userTokenPair = jwtUtil.resolveAuthTokenAndUserFromJws(request, jws,true);

                //Authenticate user on security context.
                SecurityContextHolder.getContext().setAuthentication(userTokenPair.getRight());

                //Set the current primary role
                NetworkContextImplBase.current().setPrimaryRole(userTokenPair.getLeft());
            } catch (Exception e) {
                // bl: if it fails, just render an appropriate error via our resolver
                resolver.resolveException(request, response, null, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
