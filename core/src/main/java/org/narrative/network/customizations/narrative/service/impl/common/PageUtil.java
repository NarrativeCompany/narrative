package org.narrative.network.customizations.narrative.service.impl.common;

import org.narrative.common.persistence.hibernate.criteria.CriteriaList;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.LongSupplier;

/**
 * Paging support helpers
 */
public class PageUtil {
    /**
     * Mutate an existing {@link CriteriaList} with paging criteria.
     *
     * @param criteriaList The criteria list to mutate
     * @param pageRequest  The page request from which to extract paging criteria.
     */
    public static void mutateCriteriaListWithPagingCriteria(CriteriaList criteriaList, Pageable pageRequest) {
        //CriteriaList pages start at 1
        criteriaList.setPage(pageRequest.getPageNumber() + 1);
        criteriaList.doSetRowsPerPage(pageRequest.getPageSize());
        criteriaList.doCount(true);
    }

    /**
     * Constructs a {@link PageDataDTO} based on the given {@code content}, {@link Pageable}
     *
     * This is a slightly modified version of {@link PageableExecutionUtils#getPage(List, Pageable, LongSupplier)} that
     * utilizes {@link PageDataDTO} instead of {@link PageImpl}
     *
     * @param content  The list of data to wrap in a {@link Page} - must not be {@literal null}.
     * @param pageable The pageable to use - must not be {@literal null}.
     * @param total    Total count of results
     * @return the {@link PageDataDTO}.
     */
    public static <T> PageDataDTO<T> buildPage(List<T> content, Pageable pageable, long total) {
        return new PageDataDTO<>(buildPageImpl(content, pageable, total));
    }

    public static <T> Page<T> buildPageImpl(List<T> content, Pageable pageable, long total) {
        Assert.notNull(content, "Content must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        if (pageable.isUnpaged() || pageable.getOffset() == 0) {

            if (pageable.isUnpaged() || pageable.getPageSize() > content.size()) {
                return new PageImpl<>(content, pageable, content.size());
            }
            return new PageImpl<>(content, pageable, total);
        }

        if (content.size() != 0 && pageable.getPageSize() > content.size()) {
            return new PageImpl<>(content, pageable, pageable.getOffset() + content.size());
        }

        return new PageImpl<>(content, pageable, total);
    }
}
