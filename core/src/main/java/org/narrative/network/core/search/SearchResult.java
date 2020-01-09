package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import java.sql.Timestamp;

/**
 * Date: Sep 22, 2008
 * Time: 7:45:51 AM
 *
 * @author brian
 */
public interface SearchResult {
    public OID getOid();

    public int getResultIndex();

    public Timestamp getLiveDatetime();

    public AuthorProvider getAuthorProvider();

    public IndexType getIndexType();

    public boolean isValidSearchResult();

    public Object getIdForManageContent();

    /**
     * allow specified search results to be vetoed either due to security (through PrimaryRole),
     * through status of an object (e.g. deleted user/area), or through non-existence of
     * object in our database
     *
     * @param primaryRole the current user
     * @return true if this result item should be vetoed.  false if it should not.
     */
    public boolean veto(PrimaryRole primaryRole);
}
