package org.narrative.network.core.search.services;

import java.util.List;

/**
 * Date: 1/24/13
 * Time: 1:27 PM
 * User: jonmark
 */
public class SearchPaginationParams {
    private List<Integer> maxIndex;
    private List<Long> maxDate;

    private IndexSearcherTask.IndexSort sort = IndexSearcherTask.IndexSort.SCORE;

    private int page = 1;
    private int rowsPerPage = 50;

    public List<Integer> getMaxIndex() {
        return maxIndex;
    }

    public void setMaxIndex(List<Integer> maxIndex) {
        this.maxIndex = maxIndex;
    }

    public List<Long> getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(List<Long> maxDate) {
        this.maxDate = maxDate;
    }

    public IndexSearcherTask.IndexSort getSort() {
        return sort;
    }

    public void setSort(IndexSearcherTask.IndexSort sort) {
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }
}
