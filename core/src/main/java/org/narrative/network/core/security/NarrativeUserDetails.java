package org.narrative.network.core.security;

import org.narrative.common.persistence.OID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Value
public class NarrativeUserDetails implements UserDetails {
    /**
     * Wrap the User and delegate
      */
    @Getter(AccessLevel.NONE)
    private final UserDetails userDetails;
    private final OID userOID;
    private final boolean twoFactorAuthenticationEnabled;

    @Builder
    public NarrativeUserDetails(UserDetails userDetails, OID userOID, boolean twoFactorAuthenticationEnabled) {
        this.userDetails = userDetails;
        this.userOID = userOID;
        this.twoFactorAuthenticationEnabled = twoFactorAuthenticationEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    @Override
    public String getPassword() {
        return userDetails.getPassword();
    }

    /**
     * Delegate to #getUserName - this is a synonym.
     */
    public String getEmailAddress() {
        return userDetails.getUsername();
    }

    /**
     * This field is populated with email address since that is the equivalent of user name for our purposes
     */
    @Override
    public String getUsername() {
        return userDetails.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return userDetails.isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userDetails.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userDetails.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userDetails.isCredentialsNonExpired();
    }
}
