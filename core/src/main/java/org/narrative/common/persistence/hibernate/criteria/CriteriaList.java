package org.narrative.common.persistence.hibernate.criteria;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.util.TaskInterface;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 2, 2006
 * Time: 5:37:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CriteriaList<T extends DAOObject, S extends CriteriaSort> extends TaskInterface<List<T>> {
    public int getPage();

    public void setPage(int page);

    public int getRowsPerPage();

    public void doSetRowsPerPage(int rowsPerPage);

    public S getSort();
    // bl: struts/xwork isn't smart enough to figure this out, so we can't define the setSort in the interface.
    // with this defined, struts/xwork thinks the actual class to set is CriteriaSort (not the actual concrete/passed
    // parameter) and thus can't set the real enum value.
    //public void setSort(S sort);

    public boolean isSortAsc();

    public void setSortAsc(boolean sortAsc);

    public void doCount(boolean doCount);

    public Integer getCount();
}
