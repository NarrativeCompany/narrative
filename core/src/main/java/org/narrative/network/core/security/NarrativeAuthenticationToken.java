package org.narrative.network.core.security;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Post-authentication token which contains the {@link org.narrative.network.core.security.NarrativeUserDetails}
 * generated during authentication.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NarrativeAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -1L;
    private final NarrativeUserDetails principal;

    @Builder
    public NarrativeAuthenticationToken(Collection<? extends GrantedAuthority> authorities, NarrativeUserDetails principal, boolean isAuthenticated) {
        super(authorities);
        this.principal = principal;
        this.setAuthenticated(isAuthenticated);
    }

    @Override
    public String getCredentials() {
        return principal != null ? principal.getPassword() : null;
    }
}
