package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.search.AreaDataIndexHandlerBase;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.MessageSearchResult;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.core.search.SearchResultImpl;
import org.narrative.network.core.search.UserSearchResult;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchCriteria;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchResult;
import org.narrative.network.customizations.narrative.publications.services.PublicationSearchCriteria;
import org.narrative.network.customizations.narrative.publications.services.PublicationSearchResult;
import org.narrative.network.customizations.narrative.security.DataVisibilityContextManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2/23/11
 * Time: 11:35 AM
 *
 * @author brian
 */
public class AreaSearcherTask extends IndexSearcherTask<SearchResultImpl> {

    private final Portfolio portfolio;

    public AreaSearcherTask(SearchCriteria criteria, SearchPaginationParams paginationParams, Portfolio portfolio) {
        super(criteria, paginationParams);
        this.portfolio = portfolio;
    }

    @Override
    protected void appendExtraQueryParams(BooleanQuery booleanQuery) {
        Area area = getAreaContext().getArea();
        assert isEqual(portfolio.getArea(), area) : "Area mismatch for portfolio and current area!";

        // bl: no need to filter searches by area in narrative. there's only ever one area, so this is unnecessary.
        /*TermQuery areaTermQuery = OidSearchUtil.getTermQuery(area.getOid(), AreaDataIndexHandlerBase.FIELD__COMMON__AREA_OID);
        // bl: no need to boost based on area
        areaTermQuery.setBoost(0);
        booleanQuery.add(areaTermQuery, BooleanClause.Occur.MUST);*/

        // jw: Ideally this code would go into the SearchCriteria, but we want to filter using the content types available
        //     to this area.  No point making solr have to do extra work when filtering.
        // if we are including content we need to filter down to the content types that are currently supported. Since
        // we could have old Blog posts in a index on a site that no longer has the blog enabled.
        boolean searchContentReplies = isSearchingIndexType(IndexType.REPLY) && isSearchingReplyCompositionType(CompositionType.CONTENT);
        if ((isSearchingIndexType(IndexType.CONTENT) || searchContentReplies)) {
            // bl: we don't care about this for narrative. it adds a ton of unnecessary contentType negative matches
            // to solr search queries. leaving it off to keep the queries cleaner, which should be less work for solr.
            /*Collection<ContentType> excludedContentTypes = CollectionUtils.disjunction(getCriteria().getContentTypes(), area.getAreaType().getDefaultAllowedContentTypes());
            if (excludedContentTypes != null) {
                for (ContentType excludedContentType : excludedContentTypes) {
                    TermQuery excludedContentTypeQuery = excludedContentType.getTermQuery();
                    // bl: don't want extra relevance added for documents that match by ContentType
                    excludedContentTypeQuery.setBoost(0);
                    booleanQuery.add(excludedContentTypeQuery, BooleanClause.Occur.MUST_NOT);
                }
            }*/
        }

        // Filter on data visibility if necessary
        if (DataVisibilityContextManager.isRestrictedContentFiltered()) {
            BooleanQuery ageRatingFilter = new BooleanQuery();
            for (AgeRating ageRating: DataVisibilityContextManager.getPermittedAgeRatingSet()) {
                ageRatingFilter.add(new TermQuery(new Term(AreaDataIndexHandlerBase.FIELD__COMMON__AGE_RATING, Integer.toString(ageRating.getId()))), BooleanClause.Occur.SHOULD);
            }
            booleanQuery.add(ageRatingFilter, BooleanClause.Occur.MUST);
        }
    }

    protected boolean isSearchingIndexType(IndexType indexType) {
        return getCriteria().getIndexTypes().contains(indexType);
    }

    protected boolean isSearchingReplyCompositionType(CompositionType consumerType) {
        return getCriteria().getReplyCompositionTypes().contains(consumerType);
    }

    @Override
    protected void prefetchResultChunk(List<SearchResultImpl> searchResults) {
        prefetchSearchResults(searchResults);
    }

    public static void prefetchSearchResults(List<SearchResultImpl> searchResults) {
        // jw: in order to pre-fetch the results, lets group the results by their logical type and get them all at the
        //     same time, I am abstracting this out from the individual searches so that we are only ever doing this in
        //     one place.
        List<MessageSearchResult> messageResults = newLinkedList();
        List<UserSearchResult> userResults = newLinkedList();
        List<NicheSearchResult> nicheResults = newLinkedList();
        List<PublicationSearchResult> publicationResults = newLinkedList();

        for (SearchResult searchResult : searchResults) {
            if (searchResult instanceof MessageSearchResult) {
                messageResults.add((MessageSearchResult) searchResult);

            } else if (searchResult instanceof UserSearchResult) {
                userResults.add((UserSearchResult) searchResult);

            } else if (searchResult instanceof NicheSearchResult) {
                nicheResults.add((NicheSearchResult) searchResult);

            } else if (searchResult instanceof PublicationSearchResult) {
                publicationResults.add((PublicationSearchResult) searchResult);
            }
        }

        if (!userResults.isEmpty()) {
            UserSearchCriteria.prefetchUserResultChunk(userResults);
        }

        if (!nicheResults.isEmpty()) {
            NicheSearchCriteria.prefetchNicheResultChunk(nicheResults);
        }

        if (!publicationResults.isEmpty()) {
            PublicationSearchCriteria.prefetchPublicationResultChunk(publicationResults);
        }

        if (!messageResults.isEmpty()) {
            Map<CompositionType, Set<OID>> groupedConsumerOids = ContentSearchCriteria.getRealmPartitionOidToConsumerOids(messageResults);
            final Map<OID, ? extends CompositionConsumer> consumerOidToConsumer = ContentSearchCriteria.getCompositionOidToConsumerMapForData(groupedConsumerOids);

            ContentSearchCriteria.prefetchMessageResultChunk(consumerOidToConsumer, messageResults);
        }
    }

    @Override
    protected void finalizeSearchResults(List<SearchResultImpl> searchResultList) {
        List<MessageSearchResult> messageResults = newLinkedList();
        for (SearchResultImpl searchResult : searchResultList) {
            if (searchResult instanceof MessageSearchResult) {
                messageResults.add((MessageSearchResult) searchResult);
            }
        }

        if (!messageResults.isEmpty()) {
            ContentSearchCriteria.finalizeMessageSearchResults(messageResults, getCriteria());
        }
    }

}
