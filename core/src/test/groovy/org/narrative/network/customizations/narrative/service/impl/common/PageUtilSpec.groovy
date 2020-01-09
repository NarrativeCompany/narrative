package org.narrative.network.customizations.narrative.service.impl.common

import org.springframework.data.domain.Pageable
import spock.lang.Specification
import spock.lang.Unroll

class PageUtilSpec extends Specification {
    @Unroll
    def "getNumberOfElements always returns the correct number of total elements in the List"() {
        given:
            def pageableMock = Spy(Pageable)
            def content = new ArrayList<Integer>()

            (1..contentSize).each {
                content.add(it)
            }

            pageableMock.isUnpaged() >> false
            pageableMock.getPageSize() >> pageSize
            pageableMock.getOffset() >> pageSize * pageNumber
            pageableMock.getPageNumber() >> pageNumber

        when:
            def page = PageUtil.buildPage(content, pageableMock, contentSize)

        then:
            page.info.getNumberOfElements().equals(result)

        where:
            contentSize | pageSize | pageNumber || result
            6           | 2        | 0          || 6
            6           | 2        | 1          || 6
            6           | 2        | 2          || 6
            6           | 2        | 3          || 6
            6           | 10       | 0          || 6
            60          | 100      | 0          || 60
            60          | 100      | 1          || 60
            60          | 100      | 2          || 60
            20          | 20       | 0          || 20

    }
}
