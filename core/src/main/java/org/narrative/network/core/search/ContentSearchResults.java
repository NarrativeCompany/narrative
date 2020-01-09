package org.narrative.network.core.search;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 22, 2006
 * Time: 9:50:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContentSearchResults {
    private final Collection<ContentSearchResult> results;
    private final int totalResults;

    public ContentSearchResults(int totalResults, Collection<ContentSearchResult> results) {
        this.totalResults = totalResults;
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public Collection<ContentSearchResult> getResults() {
        return results;
    }
}