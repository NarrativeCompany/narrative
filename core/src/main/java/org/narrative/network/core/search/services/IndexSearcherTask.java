package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermRangeQuery;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2/21/11
 * Time: 8:49 AM
 *
 * @author brian
 */
public abstract class IndexSearcherTask<T extends SearchResult> extends AreaTaskImpl<List<T>> {
    public static final String MAX_INDEX_PARAM = "searchPagination.maxIndex";
    public static final String MAX_DATE_PARAM = "searchPagination.maxDate";

    public static enum IndexSort {
        SCORE,
        ITEM_DATE;

        public String getNameForDisplay() {
            return wordlet("indexSort." + this);
        }

        public boolean isScore() {
            return this == SCORE;
        }

        public boolean isItemDate() {
            return this == ITEM_DATE;
        }
    }

    private final SearchCriteria criteria;
    private final SearchPaginationParams paginationParams;

    private boolean returnRawResults;

    private boolean allowNoneFilteredSearches;

    private long totalResults;
    private String prevPageUrl;
    private String nextPageUrl;
    private Long nextPageMaxDate;
    private Integer lastResultIndexOnPage;

    public IndexSearcherTask(SearchCriteria criteria, SearchPaginationParams paginationParams) {
        super(false);
        this.criteria = criteria;
        this.paginationParams = paginationParams;
    }

    protected abstract void appendExtraQueryParams(BooleanQuery booleanQuery);

    protected abstract void prefetchResultChunk(List<T> searchResults);

    protected abstract void finalizeSearchResults(List<T> searchResultList);

    @Override
    protected final List<T> doMonitoredTask() {
        if (!allowNoneFilteredSearches && !criteria.isCriteriaSpecified()) {
            return null;
        }

        final TreeSet<Integer> maxIndexSorted = new TreeSet<>(Collections.reverseOrder());
        if (!isEmptyOrNull(paginationParams.getMaxIndex())) {
            // jw: We had a spider request (Baidu Bot) that requested a tag search page where one of the max index values
            //     did not have a value.  That resulted in a NPE here.  Auditing our code it doesnt seem like there is a
            //     way for us to have generated that URL.
            paginationParams.getMaxIndex().remove(null);

            maxIndexSorted.addAll(paginationParams.getMaxIndex());
        }

        final TreeSet<Long> maxDateSorted = new TreeSet<>();

        boolean hasMoreResults = false;
        lastResultIndexOnPage = null;
        long maxDateOnNextPage = Long.MAX_VALUE;

        BooleanQuery booleanQuery = new BooleanQuery();
        // bl: always limit searches to the current auth zone
        // bl: there is only one auth zone for narrative, so leaving this off as it's unnecessary
        //booleanQuery.add(new TermQuery(new Term(IndexHandler.FIELD__COMMON__AUTH_ZONE, getNetworkContext().getAuthZone().getOid().toString())), BooleanClause.Occur.MUST);

        criteria.appendQueryParams(booleanQuery);

        appendExtraQueryParams(booleanQuery);

        Integer start = null;
        if (paginationParams.getSort().isScore()) {
            start = 0;
            if (!maxIndexSorted.isEmpty()) {
                start = maxIndexSorted.first();
            }
            lastResultIndexOnPage = start;
        } else {
            assert paginationParams.getSort().isItemDate() : "Currently only support score and date sorting!";
            if (paginationParams.getMaxDate() != null) {
                maxDateSorted.addAll(paginationParams.getMaxDate());
            }

            if (!maxDateSorted.isEmpty()) {
                Timestamp maxDateToUse = new Timestamp(maxDateSorted.first());
                maxDateOnNextPage = maxDateToUse.getTime() - 1;

                TermRangeQuery termRangeQuery = new TermRangeQuery(IndexHandler.FIELD__COMMON__ITEM_DATE, null, SearchCriteria.DATE_FORMAT.format(maxDateToUse), false, false);
                // bl: don't want to boost by the range query
                termRangeQuery.setBoost(0);
                booleanQuery.add(termRangeQuery, BooleanClause.Occur.MUST);
            }
        }

        paginationParams.setPage(Math.max(paginationParams.getPage(), 1));

        final int maxResults = paginationParams.getRowsPerPage() * 5;

        final List<T> searchResultList = newArrayList(paginationParams.getRowsPerPage());
        outer:
        for (int i = 0; i < 10; i++) {
            SearchResults searchResults = IndexHandler.search(criteria.getIndexTypes(), booleanQuery, paginationParams.getSort().isItemDate(), maxResults, start, false);

            totalResults = searchResults.getTotalResults();

            List<T> resultChunk = (List<T>) searchResults.getResults();

            if (!returnRawResults) {
                prefetchResultChunk(resultChunk);
            }

            int resultIndex = 0;
            for (T searchResult : resultChunk) {
                resultIndex++;
                if (!returnRawResults) {
                    // bl: skip any search results that are not valid (e.g. in the search index, but not in db anymore).
                    if (!searchResult.isValidSearchResult()) {
                        continue;
                    }
                    // skip any result that needs to be vetoed (if we are securing results).
                    if (searchResult.veto(getNetworkContext().getPrimaryRole())) {
                        continue;
                    }
                }
                searchResultList.add(searchResult);
                if (searchResultList.size() == paginationParams.getRowsPerPage()) {
                    // jw: If we have hit our limit, then we could still have more results if we still have some in our
                    //     current bucket to process, or if there are still some on the server left to process.
                    if ((resultChunk.size() - resultIndex > 0) || totalResults > maxResults) {
                        hasMoreResults = true;
                    }
                    break outer;
                }
            }
            // if we've run out of search results, then break out
            if (resultChunk.size() < maxResults) {
                break;
            }
            //ok we need more results so figure out the next score to use as the max
            if (paginationParams.getSort().isScore()) {
                // bl: use the the last result index for the next chunk
                Integer searchResultIndex = resultChunk.get(resultChunk.size() - 1).getResultIndex();
                if (searchResultIndex < start) {
                    throw UnexpectedError.getRuntimeException("Found a searchResultIndex value (" + searchResultIndex + ") that was higher than the current start (" + start + ")!  This shouldn't be possible!");
                }
                start = searchResultIndex;
            }
        }

        if (!searchResultList.isEmpty()) {
            SearchResult lastSearchResult = searchResultList.get(searchResultList.size() - 1);
            lastResultIndexOnPage = lastSearchResult.getResultIndex();
            maxDateOnNextPage = lastSearchResult.getLiveDatetime().getTime();
        }

        if (!returnRawResults) {
            finalizeSearchResults(searchResultList);
        }

        TreeSet<?> maxIndexSetSorted = paginationParams.getSort().isItemDate() ? maxDateSorted : maxIndexSorted;
        String searcherParam = paginationParams.getSort().isItemDate() ? MAX_DATE_PARAM : MAX_INDEX_PARAM;
        String nextPageMax = paginationParams.getSort().isItemDate() ? Long.toString(maxDateOnNextPage) : lastResultIndexOnPage.toString();
        nextPageMaxDate = paginationParams.getSort().isItemDate() ? maxDateOnNextPage : null;

        ObjectPair<String, String> pair = IndexSearcherTask.getPrevNextUrls(maxIndexSetSorted, searcherParam, nextPageMax, hasMoreResults);
        prevPageUrl = pair.getOne();
        nextPageUrl = pair.getTwo();

        paginationParams.setPage(maxIndexSetSorted.size() + 1);

        return searchResultList;
    }

    public static List<OID> getOidsFromSearchResults(List<? extends SearchResult> searchResults) {
        List<OID> ret = newArrayList(searchResults.size());
        for (SearchResult searchResult : searchResults) {
            ret.add(searchResult.getOid());
        }
        return ret;
    }

    public static ObjectPair<String, String> getPrevNextUrls(TreeSet<?> maxSetSorted, String searcherParam, String nextPageMax, boolean hasMoreResults) {
        return getPrevNextUrls(maxSetSorted, searcherParam, nextPageMax, hasMoreResults, "");
    }

    /**
     * This method will take a set of objects and a param and then generate previous
     * and next page URLs that allow you to page between results when you do not know
     * the total number of results ahead of time.
     * <p>
     * (e.g., &maxDate=5&maxDate=4&maxDate=3)
     *
     * @param maxSetSorted   The TreeSet containing the 'max' values (descending)
     * @param searcherParam  The param name for the 'max' values
     * @param nextPageMax    The most recent 'max' value; not present in maxSetSorted
     * @param hasMoreResults Whether or not there are potentially more results to show
     * @return An ObjectPair of the two pagination URLs (previous, next)
     */
    public static ObjectPair<String, String> getPrevNextUrls(TreeSet<?> maxSetSorted, String searcherParam, String nextPageMax, boolean hasMoreResults, String preParameterUrlSuffix) {
        String prevPageUrl = "";
        String nextPageUrl = "";

        if (hasMoreResults || !maxSetSorted.isEmpty()) {
            //first remove all the max score parameters
            StringBuilder newUrl = new StringBuilder();
            String oldUrl = networkContext().getReqResp().getUrl();
            boolean addAmpersand = false;
            int start = oldUrl.indexOf("?");
            if (start != -1) {
                start++;
                newUrl.append(oldUrl.substring(0, start));
                //sb: only append the preParmeterUrlSuffix if it is not already there
                if (!isEmpty(preParameterUrlSuffix) && newUrl.indexOf(preParameterUrlSuffix) < 0) {
                    newUrl.append(preParameterUrlSuffix);
                }
                String[] params = oldUrl.substring(start).split("&");
                for (String parm : params) {
                    if (isEmpty(parm) || parm.startsWith(searcherParam)) {
                        continue;
                    }
                    if (addAmpersand) {
                        newUrl.append("&");
                    }
                    addAmpersand = true;
                    newUrl.append(parm);
                }
            } else {
                newUrl.append(oldUrl);
                //sb: only append the preParmeterUrlSuffix if it is not already there
                if (!isEmpty(preParameterUrlSuffix) && newUrl.indexOf(preParameterUrlSuffix) < 0) {
                    newUrl.append(preParameterUrlSuffix);
                }
                newUrl.append("?");
            }

            //if there is a previous page, then create the previous page url
            Object[] maxItems = maxSetSorted.toArray(new Object[]{});
            if (maxItems.length > 0) {
                // sb: start from maxItems[1] to avoid the current page's value when calculating the previous url
                for (int i = 1; i <= maxItems.length - 1; i++) {
                    if (addAmpersand) {
                        newUrl.append("&");
                    }
                    addAmpersand = true;
                    newUrl.append("&").append(searcherParam).append("=").append(maxItems[i]);
                }
                prevPageUrl = newUrl.toString();

                // sb: maxItems[0] will always be the current page's score, so we want that added after we calculate the
                // previous url
                if (addAmpersand) {
                    newUrl.append("&");
                }
                addAmpersand = true;
                newUrl.append(searcherParam).append("=").append(maxItems[0]);
            }

            //if there is a next page, then create the next page url
            if (hasMoreResults) {
                if (addAmpersand) {
                    newUrl.append("&");
                }
                newUrl.append(searcherParam).append("=").append(nextPageMax);
                nextPageUrl = newUrl.toString();
            }
        }

        return new ObjectPair<>(prevPageUrl, nextPageUrl);
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }

    public SearchPaginationParams getPaginationParams() {
        return paginationParams;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public String getPrevPageUrl() {
        return prevPageUrl;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public Long getNextPageMaxDate() {
        return nextPageMaxDate;
    }

    public Integer getLastResultIndexOnPage() {
        return lastResultIndexOnPage;
    }

    public void setAllowNoneFilteredSearches(boolean allowNoneFilteredSearches) {
        this.allowNoneFilteredSearches = allowNoneFilteredSearches;
    }

    public void setReturnRawResults(boolean returnRawResults) {
        this.returnRawResults = returnRawResults;
    }
}
