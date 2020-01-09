package org.narrative.network.core.security;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.shared.security.Guest;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

@Service
public class NarrativeUserDetailsServiceImpl implements NarrativeUserDetailsService {

    private final AreaTaskExecutor areaTaskExecutor;

    public NarrativeUserDetailsServiceImpl(AreaTaskExecutor areaTaskExecutor) {
        this.areaTaskExecutor = areaTaskExecutor;
    }

    /**
     * Locates user by email address.  Since we are leveraging the default authentication infrastructure
     * including {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider} this is the
     * method that will be called during execution of {@link EmailAndPasswordAuthenticationFilter} which ensures
     * the parameter passed is the user's email.
     *
     * @param emailAddress for user.
     * @return Populated {@link UserDetails} on successful identification.
     * @throws UsernameNotFoundException when user name is null or empty.
     */
    @Override
    public NarrativeUserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(emailAddress)) {
            throw new UsernameNotFoundException("User email address is NULL.");
        }

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<NarrativeUserDetails>() {
            @Override
            protected NarrativeUserDetails doMonitoredTask() {
                User user = User.dao().getByPrimaryEmailAddress(emailAddress);

                if (user == null) {
                    throw new UsernameNotFoundException("User lookup by email address " + emailAddress + " - user not found.");
                }
                return buildUserDetails(user);
            }
        });


    }

    /**
     * This method is used during subsequent JWT parsing/validation.
     *
     * @param oid for user.
     * @return UserDetails Populated {@link UserDetails} on successful identification.
     * @throws UserPrincipalNotFoundException No user match for oid.
     */
    @Override
    public NarrativeUserDetails loadUserByOid(OID oid) throws UserPrincipalNotFoundException {

        if (oid == null) {
            throw new UserPrincipalNotFoundException("User OID is null.");
        }

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<NarrativeUserDetails>() {
            @Override
            protected NarrativeUserDetails doMonitoredTask() {
                User user = User.dao().get(oid);
                if (!exists(user)) {
                    throw new UsernameNotFoundException("User lookup by OID " + oid + " - user not found.");
                }
                return buildUserDetails(user);
            }
        });
    }

    /**
     * Build guest {@link User} to guest {@link UserDetails}.
     *
     * @param guest User.
     * @return Populated guest UserDetails.
     */
    @Override
    public NarrativeUserDetails buildGuestUser(@NonNull Guest guest) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("Guest")
                .password("")
                .roles("GUEST")
                .build();
        //Build a Narrative user that delegates to UserDetails so we can add extra properties
        return NarrativeUserDetails.builder()
                .userDetails(userDetails)
                .twoFactorAuthenticationEnabled(false)
                .build();
    }

    /**
     *  Utility method to build {@link UserDetails} based on {@link User}.
     *
     * @param user User object.
     * @return Populated UserDetails object.
     */
    private NarrativeUserDetails buildUserDetails(User user) {
        // TODO: Role / Permission model yet to be determined. Default to ROLE_USER temporarily.
        List<String> authProviders = new ArrayList<>(Collections.singletonList("USER"));

        boolean isTwoFactorAuthEnabled = user.isTwoFactorAuthenticationEnabled();

        String roles = String.join(",", authProviders);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                //Email address is equivalent to user name for Narrative.
                //{@link NarrativeUserDetails#getEmailAddress} is a synonym for this field
                .withUsername(user.getEmailAddress())
                //Store the password hash to be used during JWT generation and validation
                .password(user.getInternalCredentials().getPasswordFields().getHashedPassword())
                .roles(roles)
                .build();
        //Build a Narrative user that delegates to UserDetails so we can add extra properties
        return NarrativeUserDetails.builder()
                .userDetails(userDetails)
                .userOID(user.getOid())
                .twoFactorAuthenticationEnabled(isTwoFactorAuthEnabled)
                .build();
    }
}
