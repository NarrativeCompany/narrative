package org.narrative.network.core.security;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.Guest;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.file.attribute.UserPrincipalNotFoundException;

public interface NarrativeUserDetailsService extends UserDetailsService {
    /**
     * Locates user by email address.
     *
     * @param emailAddress for user.
     * @return Populated {@link UserDetails} on successful identification.
     * @throws UsernameNotFoundException when user name is null or empty.
     */
    @Override
    NarrativeUserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException;

    /**
     * Locates user by oid.
     *
     * @param oid for user.
     * @return UserDetails Populated {@link UserDetails} on successful identification.
     * @throws UserPrincipalNotFoundException No user match for oid.
     */
    NarrativeUserDetails loadUserByOid(OID oid) throws UserPrincipalNotFoundException;

    /**
     * Build guest {@link User} to guest {@link UserDetails}.
     *
     * @param guest User.
     * @return Populated guest UserDetails.
     */
    NarrativeUserDetails buildGuestUser(@NonNull Guest guest);
}
