package org.narrative.common.util;

import org.narrative.common.persistence.ObjectPair;

import java.util.ArrayList;
import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 26, 2006
 * Time: 5:09:20 PM
 * This class is used to help calcualate all the things you might need in the ui for pagination.
 */
public class PaginationHelper {
    private int itemCount;
    private int itemsPerPage;
    private int currentPage;

    private List<ClassicPaginationPage> classicPaginationPages;

    /**
     * @deprecated This constructor is here for jsp:useBean only
     */
    public PaginationHelper() {
    }

    public PaginationHelper(int itemCount, int itemsPerPage, int currentPage) {
        this.itemCount = itemCount;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = currentPage;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getFirstItemOnPage() {
        return ((currentPage - 1) * itemsPerPage) + 1;
    }

    public int getTotalPages() {
        if (itemCount == 0) {
            return 1;
        }
        return (int) Math.ceil((float) itemCount / itemsPerPage);
    }

    public int getLastItemOnPage() {
        if (isLastPage()) {
            return itemCount;
        } else {
            return currentPage * itemsPerPage;
        }
    }

    public boolean isFirstPage() {
        return currentPage == 1;
    }

    public boolean isLastPage() {
        return currentPage == getTotalPages();
    }

    public boolean isHasNextPage() {
        return currentPage < getTotalPages();
    }

    public boolean isHasPrevPage() {
        return currentPage > 1;
    }

    public List<ClassicPaginationPage> getClassicPaginationPages() {
        if (classicPaginationPages != null) {
            return classicPaginationPages;
        }

        int currentPage = getCurrentPage();
        int totalPages = getTotalPages();
        classicPaginationPages = new ArrayList<ClassicPaginationPage>();

        if (currentPage == -1) {
            classicPaginationPages = doPageFirstDotLast(totalPages);
        } else {
            classicPaginationPages = doPageFirstDotCurrentDotLast(currentPage, totalPages);
        }

        return classicPaginationPages;
    }

    private static List<ClassicPaginationPage> doPageOneThroughN(int n) {
        List<ClassicPaginationPage> classicPaginationPages = new ArrayList<ClassicPaginationPage>();

        for (int i = 1; i <= n; i++) {
            classicPaginationPages.add(new ClassicPaginationPage(i, String.valueOf(i)));
        }
        return classicPaginationPages;
    }

    private static List<ClassicPaginationPage> doPageFirstDotCurrentDotLast(int currentPage, int totalPages) {
        List<ClassicPaginationPage> classicPaginationPages = new ArrayList<ClassicPaginationPage>();

        if (totalPages <= 10) {
            return doPageOneThroughN(totalPages);
        }

        if (currentPage - 4 > 0) {
            classicPaginationPages.add(new ClassicPaginationPage(1, "1"));
        }

        if ((Math.ceil((double) (currentPage - 1) / 2)) < (currentPage - 3)) {
            classicPaginationPages.add(new ClassicPaginationPage((int) Math.ceil((double) (currentPage - 1) / 2), wordlet("pagination.ellipsis")));
        }

        for (int i = 3; i > 0; i--) {
            if (currentPage - i > 0) {
                classicPaginationPages.add(new ClassicPaginationPage((currentPage - i), String.valueOf((currentPage - i))));
            }
        }

        classicPaginationPages.add(new ClassicPaginationPage(currentPage, null));

        for (int i = 1; i <= 3; i++) {
            if (currentPage + i <= totalPages) {
                classicPaginationPages.add(new ClassicPaginationPage((currentPage + i), String.valueOf((currentPage + i))));
            }
        }

        if ((currentPage + Math.ceil((double) (totalPages - currentPage) / 2)) > (currentPage + 3)) {
            classicPaginationPages.add(new ClassicPaginationPage((int) (currentPage + Math.ceil((double) (totalPages - currentPage) / 2)), wordlet("pagination.ellipsis")));
        }

        if (currentPage + 4 <= totalPages) {
            classicPaginationPages.add(new ClassicPaginationPage(totalPages, String.valueOf(totalPages)));
        }

        return classicPaginationPages;
    }

    private static List<ClassicPaginationPage> doPageFirstDotLast(int totalPages) {
        int halfWayLink = Math.round(totalPages / 2);

        List<ClassicPaginationPage> classicPaginationPages = new ArrayList<ClassicPaginationPage>();

        classicPaginationPages.add(new ClassicPaginationPage(1, "1"));
        classicPaginationPages.add(new ClassicPaginationPage(2, "2"));

        if (totalPages > 4) {
            classicPaginationPages.add(new ClassicPaginationPage(halfWayLink, wordlet("pagination.ellipsis")));
        }

        if (totalPages > 3) {
            classicPaginationPages.add(new ClassicPaginationPage(totalPages - 1, String.valueOf(totalPages - 1)));
        }

        if (totalPages > 2) {
            classicPaginationPages.add(new ClassicPaginationPage(totalPages, String.valueOf(totalPages)));
        }

        return classicPaginationPages;
    }

    public static class ClassicPaginationPage extends ObjectPair<Integer, String> {
        public ClassicPaginationPage(Integer one, String two) {
            super(one, two);
        }

        public Integer getPageNumber() {
            return getOne();
        }

        public String getLabel() {
            return getTwo();
        }
    }
}
