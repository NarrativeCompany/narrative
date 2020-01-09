package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Sep 19, 2008
 * Time: 2:34:26 PM
 *
 * @author brian
 */
public class UserSearchResult extends SearchResultImpl {
    private User user;

    public UserSearchResult(OID userOid, int resultIndex) {
        super(userOid, resultIndex);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.USER;
    }

    public User getUser() {
        assert isHasSetData() : "Should never attempt to get the User prior to initializing its value!";
        return user;
    }

    public void setUser(User user) {
        assert isEqual(getOid(), user.getOid()) : "User OID mismatch when setting User on search result!";
        this.user = user;
        setHasSetData(true);
    }

    @Override
    public Timestamp getLiveDatetime() {
        return getUser().getUserFields().getRegistrationDate();
    }

    @Override
    public AuthorProvider getAuthorProvider() {
        return null;
    }

    public boolean veto(PrimaryRole primaryRole) {
        // veto the user search result if the user is not visible.
        return !exists(getUser()) || !getUser().isVisible();
    }
}
