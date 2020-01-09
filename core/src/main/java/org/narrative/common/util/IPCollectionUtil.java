package org.narrative.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: Oct 13, 2005
 * Time: 10:21:08 AM
 *
 * @author Brian
 */
public class IPCollectionUtil {

    public static <T> Collection<Collection<T>> getManageableCollectionsFromCollection(Collection<T> collection, int maxItemsPerCollection) {
        Collection<Collection<T>> ret = new LinkedList<Collection<T>>();

        if (collection == null) {
            return ret;
        }
        if (collection.isEmpty()) {
            return ret;
        }
        if (maxItemsPerCollection < 1) {
            ret.add(collection);
            return ret;
        }
        if (collection.size() < maxItemsPerCollection) {
            ret.add(collection);
            return ret;
        }

        int i = 0;
        Collection<T> current = null;
        for (T item : collection) {
            // end of the current collection?  then start a new collection.
            if (i == maxItemsPerCollection) {
                i = 0;
            }
            if (i == 0) {
                ret.add(current = new LinkedList<T>());
            }
            current.add(item);
            i++;
        }

        return ret;
    }

    public static <T> List<T> getListFromArray(T[] array) {
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(array);
    }
}
