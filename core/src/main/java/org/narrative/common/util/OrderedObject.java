package org.narrative.common.util;

import org.narrative.common.persistence.OID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/15/16
 * Time: 10:01 AM
 * <p>
 * jw: I was seeing the same comparator used in a LOT of places in the code, so I decided to create this interface to
 * unify the behavior and allow us to centralize this type of sorting.
 */
public interface OrderedObject {
    OID getOid();

    int getThreadingOrder();

    static <T extends OrderedObject> List<T> createSortedList(Collection<T> objects) {
        if (isEmptyOrNull(objects)) {
            return Collections.emptyList();
        }
        List<T> orderedObjects = new ArrayList<>(objects);
        sort(orderedObjects);

        return orderedObjects;
    }

    static <T extends OrderedObject> void sort(List<T> objects) {
        // now, lets organize the files
        Collections.sort(objects, new Comparator<T>() {
            public int compare(T o1, T o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                int ret = Integer.valueOf(o1.getThreadingOrder()).compareTo(o2.getThreadingOrder());
                if (ret != 0) {
                    return ret;
                }
                return OID.compareOids(o1.getOid(), o2.getOid());
            }
        });
    }
}
