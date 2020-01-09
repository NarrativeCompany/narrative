package org.narrative.network.shared.services;

import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.Guest;
import org.narrative.network.shared.security.PrimaryRole;

import javax.persistence.Transient;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 8/24/11
 * Time: 1:28 PM
 *
 * @author Jonmark Weber
 */
public interface AuthorProvider {
    @Transient
    public User getAuthor();

    // bl: getUser() will return null if it's an anonymous author, so getRealAuthor() returns the "real" author for anonymous posts.
    @Transient
    public User getRealAuthor();

    @Transient
    public String getGuestNameResolved();

    @Transient
    public PrimaryRole getPrimaryRole();

    @Transient
    public PrimaryRole getRealAuthorPrimaryRole();

    public static PrimaryRole getPrimaryRole(AuthZone authZone, AuthorProvider... providers) {
        for (AuthorProvider provider : providers) {
            if (exists(provider)) {
                return provider.getPrimaryRole();
            }
        }

        return new Guest(authZone);
    }
}
