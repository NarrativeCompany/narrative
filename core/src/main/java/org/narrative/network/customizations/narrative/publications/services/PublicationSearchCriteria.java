package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.services.IndexSearcherTask;
import org.narrative.network.core.search.services.SearchCriteria;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchCriteria;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-07
 * Time: 10:22
 *
 * @author jonmark
 */
public class PublicationSearchCriteria extends SearchCriteria {

    public PublicationSearchCriteria(Portfolio portfolio) {
        super(portfolio, EnumSet.of(IndexType.PUBLICATION), null);
    }

    @Override
    public void appendQueryParams(BooleanQuery booleanQuery) {
        super.appendQueryParams(booleanQuery);

        // jw: four characters or more, strip out stop words
        boolean hasSimilarName = !isEmpty(getQueryString());
        if (hasSimilarName) {
            BooleanQuery wordQuery = new BooleanQuery();

            Collection<String> terms = NicheSearchCriteria.getSearchTerms(getQueryString(), false);
            List<String> stemmedTerms = NicheSearchCriteria.getStemmedSearchTerms(terms);

            NicheSearchCriteria.addTermsToQuery(wordQuery,PublicationIndexHandler.FIELD_PUBLICATION_NAME_UNSTEMMED, terms, true, 1000F);
            NicheSearchCriteria.addTermsToQuery(wordQuery, PublicationIndexHandler.FIELD__COMMON__NAME, stemmedTerms, false, 250F);
            NicheSearchCriteria.addTermsToQuery(wordQuery, IndexHandler.FIELD__COMMON__FULL_TEXT, stemmedTerms, false, 5F);

            booleanQuery.add(wordQuery, BooleanClause.Occur.MUST);
        }
    }

    @Override
    protected void addQueryStringQuery(BooleanQuery booleanQuery, Map<String, Float> fieldsToSearch) {
        // do nothing! we'll handle the queryString ourselves above
    }

    public static void prefetchPublicationResultChunk(List<PublicationSearchResult> searchResults) {
        List<OID> publicationOids = IndexSearcherTask.getOidsFromSearchResults(searchResults);
        Map<OID, Publication> oidToPublication = Publication.dao().getIDToObjectsFromObjects(Publication.dao().getObjectsFromIDsWithCache(publicationOids));
        for (PublicationSearchResult searchResult : searchResults) {
            Publication niche = oidToPublication.get(searchResult.getOid());
            if (exists(niche)) {
                searchResult.setPublication(niche);
            }
        }
    }
}
