package org.narrative.network.core.search.services;

import org.narrative.network.core.search.SearchResult;

import java.util.List;

/**
 * Date: Oct 15, 2009
 * Time: 10:40:35 AM
 *
 * @author Jonmark Weber
 */
public class SearchResults {
    private final List<SearchResult> results;
    private final long totalResults;

    public SearchResults(List<SearchResult> results, long totalResults) {
        this.totalResults = totalResults;
        this.results = results;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public long getTotalResults() {
        return totalResults;
    }
}
