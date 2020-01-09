package org.narrative.network.core.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.NarrativeAuthenticationToken;
import org.narrative.network.core.security.NarrativeUserDetails;
import org.narrative.network.core.security.NarrativeUserDetailsService;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.shared.util.NetworkLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JWT utilities to be shared amongst filter implementations
 */
@Component
public class JwtUtil {
    private static final NetworkLogger logger = new NetworkLogger(JwtUtil.class);
    public static final String TWO_FACTOR_AUTH_EXPIRY_CLAIM = "tfaExpiry";
    public static final String TWO_FACTOR_AUTH_SECRET_HASH_CLAIM = "tfh";
    public static final String AUTHORITIES_CLAIM = "authorities";
    public static final String AUTH_COOKIE_KEY = "Authorization";

    public static final Set<String> STANDARD_JWT_CLAIMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            Claims.ISSUER,
            Claims.SUBJECT,
            Claims.AUDIENCE,
            Claims.EXPIRATION,
            Claims.NOT_BEFORE,
            Claims.ISSUED_AT,
            Claims.ID
    )));

    private final NarrativeUserDetailsService userDetailsService;
    private final NarrativeProperties narrativeProperties;
    private final ObjectMapper objectMapper;

    public JwtUtil(NarrativeUserDetailsService userDetailsService,
                   NarrativeProperties narrativeProperties, ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.narrativeProperties = narrativeProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Extract the JWT string from the incoming request headers
     *
     * @param request The request of interest
     * @return String representing the encoded JWT token if found in the headers, null otherwise
     */
    public String extractJwtStringFromRequest(HttpServletRequest request) {
        Cookie authCookie = WebUtils.getCookie(request, AUTH_COOKIE_KEY);
        return authCookie != null ? authCookie.getValue() : null;
    }

    /**
     * Parse the {@link Jws} from the encoded JWT String
     *
     * @param jwtString Encoded JWT String
     * @return {@link Jws} represented by the JWT
     * @throws ServletException on untrusted JWT
     */
    public Jws<Claims> parseAndValidateJWTFromString(String jwtString) throws ServletException {
        Jws<Claims> jws = parseAndValidateJwtToken(jwtString);

        if (jws == null) {
            throw new ServletException("JWT issued by client could not be trusted.");
        }

        return jws;
    }

    /**
     * Resolve the {@link User} and {@link UsernamePasswordAuthenticationToken} from the Jws.  This method also
     * validates that the user/password hash is valid and performs validation for 2FA expiry if necessary
     *
     * @param request The request of interest
     * @param jws The JWT object
     * @param validate2FA true if 2FA expiry should be validated
     * @return {@link Pair} of {@link User} and {@link UsernamePasswordAuthenticationToken}
     * @throws UserPrincipalNotFoundException when the user identified in the token does not exist
     */
    public Pair<User, NarrativeAuthenticationToken> resolveAuthTokenAndUserFromJws(HttpServletRequest request, Jws<Claims> jws, boolean validate2FA) throws UserPrincipalNotFoundException {
        String oidString = jws.getBody().getId();
        OID userOID = OID.getOIDFromString(oidString);
        NarrativeUserDetails userDetails = userDetailsService.loadUserByOid(userOID);
        User user = User.dao().get(userOID);

        //Perform any post-parse validation that requires user information
        performPostParseJWTValidation(jws, user, userDetails, validate2FA);

        NarrativeAuthenticationToken authToken = NarrativeAuthenticationToken.builder()
                .principal(userDetails)
                .isAuthenticated(true)
                .authorities(userDetails.getAuthorities())
                .build();
        //Some paths do not require a full AuthenticationToken so skip this step if the request is not supplied
        if (request != null) {
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        return new ImmutablePair<>(user, authToken);
    }

    /**
     * Perform any post-parse validation here for the JWT claims
     */
    private void performPostParseJWTValidation(Jws<Claims> jws, User user, NarrativeUserDetails userDetails, boolean validate2FA) {
        // Validate that the user name and password hash is valid in the JWT
        validateAuthTokenHashedEmailAddressAndPassword(jws, userDetails);

        // Validate that the 2FA expiry is still valid if 2FA is enabled for this user
        if (validate2FA && user.isTwoFactorAuthenticationEnabled()) {
            validateTwoFactorAuthExpiry(jws, user.getTwoFactorAuthenticationSecretKey());
        }
    }

    /**
     * Parse JWT.
     *
     * @param authToken JWT
     * @return {@link Jws} {@link Claims} if token is trusted, NULL otherwise.
     */
    Jws<Claims> parseAndValidateJwtToken(String authToken) {

        Jws<Claims> claimsJws;

        if (!StringUtils.isEmpty(authToken)) {
            try {
                claimsJws = Jwts.parser()
                        .setSigningKey(narrativeProperties.getSecurity().getJwtSecretBytes())
                        .parseClaimsJws(authToken);
                return claimsJws;
            } catch (SignatureException ex) {
                logger.error("Invalid JWT signature.");
            } catch (MalformedJwtException ex) {
                logger.error("Invalid JWT token.");
            } catch (ExpiredJwtException ex) {
                logger.error("Expired JWT token.");
                throw new JwtTokenInvalidException("JWT token expired.");
            } catch (UnsupportedJwtException ex) {
                logger.error("Unsupported JWT token.");
            } catch (IllegalArgumentException ex) {
                logger.error("JWT claims string is empty.");
            }
        }
        return null;
    }

    /**
     * Validate the {@link Jws} subject (hash of username + hashed password) is a match with the database username
     * and hashed password.
     *
     * @param claimsJws parsed Jws claims.
     */
    private void validateAuthTokenHashedEmailAddressAndPassword(Jws<Claims> claimsJws, NarrativeUserDetails userDetails) {
        boolean hashMatch = BCrypt.checkpw(userDetails.getEmailAddress() + userDetails.getPassword(), claimsJws.getBody().getSubject());

        if (!hashMatch) {
            throw new JwtTokenInvalidException("JWT subject hash check failed.");
        }
    }

    /**
     * Validate whether the 2fa expiry for the token is still valid
     *
     * @param claimsJws parsed Jws claims
     * @throws JwtToken2FAExpiredException on a bad 2fa expiry
     */
    private void validateTwoFactorAuthExpiry(Jws<Claims> claimsJws, String userTwoFactorSecret){
        Date expiryDate = claimsJws.getBody().get(TWO_FACTOR_AUTH_EXPIRY_CLAIM, Date.class);
        String secretHash = claimsJws.getBody().get(TWO_FACTOR_AUTH_SECRET_HASH_CLAIM, String.class);

        if (expiryDate == null) {
            throw new JwtToken2FAExpiredException("JWT 2FA expiry is null in token");
        }

        if (secretHash == null) {
            throw new JwtToken2FAExpiredException("JWT 2FA secret hash is null in token");
        }

        if (!BCrypt.checkpw(userTwoFactorSecret, secretHash)){
            throw new JwtToken2FAExpiredException("JWT 2FA secret hash mismatch token");
        }

        Instant expiryInstant = expiryDate.toInstant();

        if (expiryInstant.isBefore(Instant.now())){
            throw new JwtToken2FAExpiredException("JWT 2FA secret at " + expiryInstant.toString());
        }
    }

    /**
     * get the JWT subject string to use
     * @param emailAddress the email address to incorporate into the subject
     * @param encryptedPassword the user's encrypted password to incorporate into the subject
     * @return a BCrypt encrypted String to use in the JTW subject
     */
    public String getJwtSubject(String emailAddress, String encryptedPassword) {
        return BCrypt.hashpw(emailAddress + encryptedPassword, BCrypt.gensalt());
    }

    /**
     * Build a String representation of a JWT token
     *
     * @param id               Token id
     * @param subject          Token subject
     * @param issuedAtDate     Issued at date
     * @param expiresDate      Expires date
     * @param additionalClaims {@link Map} of additional claims
     * @return A string representing the JWT
     */
    public String buildJWTToken(String id, String subject, Date issuedAtDate, Date expiresDate, Map<String, Object> additionalClaims) {
        return Jwts.builder()
                .setId(id)
                .setSubject(subject)
                .setIssuedAt(issuedAtDate)
                .setExpiration(expiresDate)
                .addClaims(additionalClaims)
                .signWith(SignatureAlgorithm.HS256, narrativeProperties.getSecurity().getJwtSecretBytes())
                .compact();
    }

    /**
     *  Build a token DTO from the JWT
     *
     * @param jwtString The JWT String to use
     * @param is2FAExpired true if the JWT has an expired 2FA expiry
     * @return {@link TokenDTO}
     */
    public TokenDTO buildTokenDTO(String jwtString,  boolean is2FAExpired) {
        TokenDTO.TokenDTOBuilder tokenDTOBuilder = TokenDTO.builder().token(jwtString);
        if (is2FAExpired) {
            tokenDTOBuilder.twoFactorAuthExpired(true);
        }
        return tokenDTOBuilder.build();
    }

    /**
     * Write a JWT string into the response as a JSON object
     *
     * @param response     The response object to write to
     * @param tokenDTO     The token DTO to write
     */
    public void writeTokenDTOAsJSONToResponse(HttpServletResponse response, TokenDTO tokenDTO){
        try {
            response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(CharEncoding.UTF_8);
            PrintWriter writer = response.getWriter();

            objectMapper.writeValue(writer, tokenDTO);
            writer.flush();
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed writing JWT JSON to response", e);
        }
    }

    /**
     * Write a JWT string into the response as a JSON object
     *
     * @param jwtString    The JWT string to write as JSON
     * @param response     The response object to write to
     * @param is2FAExpired true if the JSON should flag that the 2FA expiry is expired
     */
    public void writeJWTStringAsJSONToResponse(String jwtString, HttpServletResponse response, boolean is2FAExpired){
        writeTokenDTOAsJSONToResponse(response, buildTokenDTO(jwtString, is2FAExpired));
    }
}
