package org.narrative.network.core.security;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Pre-authentication token for use during the initial username/password authentication operation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NarrativePreAuthenticationToken extends UsernamePasswordAuthenticationToken {
    @Builder
    public NarrativePreAuthenticationToken(String emailAddress, String password, boolean rememberMe, Collection<? extends GrantedAuthority> authorities) {
        super(emailAddress, password, authorities);
        this.setAuthenticated(false);
        // bl: store the NarrativeLoginDetails with rememberMe flag in the authentication token's details object
        setDetails(NarrativeLoginDetails.builder().rememberMe(rememberMe).build());
    }

    public String getEmailAddress() {
        return (String) getPrincipal();
    }

    public String getPassword() {
        return (String) getCredentials();
    }
}
