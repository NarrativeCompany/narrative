package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.SearchResultImpl;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-07
 * Time: 10:17
 *
 * @author jonmark
 */
public class PublicationSearchResult extends SearchResultImpl {
    private final Timestamp creationDatetime;
    private Publication publication;

    public PublicationSearchResult(OID publicationOid, int resultIndex, Timestamp creationDatetime) {
        super(publicationOid, resultIndex);
        this.creationDatetime = creationDatetime;
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.PUBLICATION;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        assert isEqual(getOid(), publication.getOid()) : "Publication OID mismatch when setting Publication on search result!";
        this.publication = publication;
        setHasSetData(true);
    }

    @Override
    public Timestamp getLiveDatetime() {
        return creationDatetime;
    }

    @Override
    public AuthorProvider getAuthorProvider() {
        return null;
    }

    public boolean veto(PrimaryRole primaryRole) {
        return !exists(publication);
    }
}
