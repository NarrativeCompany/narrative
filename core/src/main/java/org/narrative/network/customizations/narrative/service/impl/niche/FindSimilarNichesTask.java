package org.narrative.network.customizations.narrative.service.impl.niche;

import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.search.SearchResultImpl;
import org.narrative.network.core.search.services.AreaSearcherTask;
import org.narrative.network.core.search.services.SearchPaginationParams;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchCriteria;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheSearchResult;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * A task to find similar niches.
 *
 * Date: 10/18/18
 * Time: 8:25 AM
 *
 * @author brian
 */
public class FindSimilarNichesTask extends AreaTaskImpl<List<Niche>> {
    private static final int MAX_SIMILAR_NICHES_SEARCH_COUNT = 5;

    private final String name;
    private final String description;
    private final Niche nicheToExclude;

    private int maxResults = MAX_SIMILAR_NICHES_SEARCH_COUNT;

    private Boolean searchUnstemmedNameOnly;
    private NicheStatus limitToStatus;

    /**
     * Construct an instance of the task.
     *
     * @param name The niche name to search by
     * @param description The niche description to search by
     * @param nicheToExclude The niche to exclude from results - null if searching only by name
     */
    public FindSimilarNichesTask(String name, String description, Niche nicheToExclude) {
        super(false);
        this.name = name;
        this.description = description;
        this.nicheToExclude = nicheToExclude;
    }

    /**
     * Run the task to find similar niches.
     *
     * @return {@link List} of similar {@link Niche} found
     */
    @Override
    protected List<Niche> doMonitoredTask() {
        Portfolio portfolio = getAreaContext().getPortfolio();
        NicheSearchCriteria criteria = new NicheSearchCriteria(portfolio);

        //Always search by name
        criteria.setQueryString(name);

        //If a description is provided, add to the search
        if (StringUtils.isNotEmpty(description)) {
            criteria.setSimilarDescription(description);
            criteria.setSearchNameOnly(false);
        } else {
            criteria.setSearchNameOnly(true);
        }

        //Exclude the provided niche OID
        if (exists(nicheToExclude)) {
            criteria.setExcludeNicheOID(nicheToExclude.getOid());
        }

        if (limitToStatus!=null) {
            criteria.setStatus(limitToStatus);
        }

        if (searchUnstemmedNameOnly != null) {
            criteria.setSearchUnstemmedNameOnly(searchUnstemmedNameOnly);
        }

        final List<Niche> similarNiches = new ArrayList<>();
        SearchPaginationParams pagination = new SearchPaginationParams();
        pagination.setRowsPerPage(maxResults);

        // jw: we are finally ready to run the search, so lets do this thing!
        List<SearchResultImpl> results = getAreaContext().doAreaTask(new AreaSearcherTask(criteria, pagination, portfolio));

        if (!isEmptyOrNull(results)) {
            for (SearchResultImpl result : results) {
                assert result instanceof NicheSearchResult : "All results from the search we ran should be NicheSearchResult.";
                similarNiches.add(((NicheSearchResult) result).getNiche());
            }
        }

        return similarNiches;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setSearchUnstemmedNameOnly(Boolean searchUnstemmedNameOnly) {
        this.searchUnstemmedNameOnly = searchUnstemmedNameOnly;
    }

    public void setLimitToStatus(NicheStatus limitToStatus) {
        this.limitToStatus = limitToStatus;
    }
}
