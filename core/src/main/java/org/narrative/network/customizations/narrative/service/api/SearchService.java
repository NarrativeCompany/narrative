package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.search.actions.SearchType;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultsDTO;

import java.util.List;

public interface SearchService {

    /**
     * Find all search results by the specified criteria, page size and page.
     *
     * @param keyword     Query string.
     * @param searchType  {@link SearchType} type of search.
     * @param channelOid  {@link OID} of the channel to filter search results to
     * @param startIndex  Last search index id. The position to start in a set of search results.
     * @param count       Paging information for the request.
     * @return {@link SearchResultsDTO} with results that matched the query.
     */
    SearchResultsDTO search(String keyword, SearchType searchType, OID channelOid, Integer startIndex, int count) throws IllegalArgumentException;

    List<NicheDTO> findActiveNichesByName (String name, int count) throws IllegalArgumentException;

}
