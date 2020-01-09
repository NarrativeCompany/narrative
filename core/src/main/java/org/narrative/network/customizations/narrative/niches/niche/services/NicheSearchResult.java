package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.SearchResultImpl;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/13/18
 * Time: 8:49 AM
 */
public class NicheSearchResult extends SearchResultImpl {
    private final Timestamp suggestionDatetime;
    private Niche niche;

    public NicheSearchResult(OID nicheOid, int resultIndex, Timestamp suggestionDatetime) {
        super(nicheOid, resultIndex);
        this.suggestionDatetime = suggestionDatetime;
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.NICHE;
    }

    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        assert isEqual(getOid(), niche.getOid()) : "Niche OID mismatch when setting Niche on search result!";
        this.niche = niche;
        setHasSetData(true);
    }

    @Override
    public Timestamp getLiveDatetime() {
        return suggestionDatetime;
    }

    @Override
    public AuthorProvider getAuthorProvider() {
        return null;
    }

    public boolean veto(PrimaryRole primaryRole) {
        return !exists(niche);
    }
}
