package org.narrative.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: Katoth
 * Date: Jun 16, 2005
 * Time: 1:37:12 AM
 * Basic Iterator implementation to simplify the task of iterating over chunks of a List
 * <p>
 * todo: Currently this does not properly support when changes are made to the underlying list around it.  We should
 * either find a way to support it so that if the list that is provided changes it has no effect on our iteration
 * or detect when the list has changed and throw a exception like we should.
 */
public class SubListIterator<T> implements Iterator<List<T>> {

    private static final NarrativeLogger logger = new NarrativeLogger(SubListIterator.class);

    public static final int CHUNK_LARGE = 1000;
    public static final int CHUNK_MEDIUM = 500;
    public static final int CHUNK_SMALL = 100;

    private final List<T> list;
    private final int chunkSize;

    private int currentIndex = 0;
    private List<T> lastSubList = null;

    /**
     * Creates a new SubListIterator initialized to the beginning of the provided list
     * read chunk size defaults to CHUNK_SMALL
     *
     * @param list The list that we want to iterate over the elements of
     */
    public SubListIterator(List<T> list) {
        this(list, CHUNK_SMALL);
    }

    /**
     * Creates a new SubListIterator initialized at the beginning of the provided list
     * and allows the creator to set the chunk size during instantiation
     *
     * @param list      The List that we want to iterate over chunks of its elements
     * @param chunkSize The Maximum number of elements to return in each sub list (Debug.asseted to be > 0)
     */
    public SubListIterator(List<T> list, int chunkSize) {
        Debug.assertMsg(logger, chunkSize > 0, "A SubListIterator must be instantiated to read a positive number of elements!");

        this.list = list;
        this.chunkSize = chunkSize;
    }

    /**
     * Retrieves the maximum amount of items that will be contained within
     * each sub list ccreated
     *
     * @return The maximum number of elements in the next resulting list
     */
    public int getChunkSize() {
        return chunkSize;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<T> getList() {
        return list;
    }

    /**
     * Determines if there are still elements in the list to return sub lists for
     *
     * @return Whether or not there are more sub lists
     */
    public boolean hasNext() {
        return currentIndex < list.size();
    }

    protected int getNextChunkSize() {
        return chunkSize;
    }

    /**
     * Retrieves the next sub list and prepares to read the next sub list
     *
     * @return The List containing the next chunk of elements
     */
    public List<T> next() {
        if (currentIndex >= list.size()) {
            throw new NoSuchElementException("No more elements in SubListIterator");
        }

        int nextChunkSize = getNextChunkSize();

        int readTo = Math.min(list.size(), currentIndex + nextChunkSize);
        lastSubList = list.subList(currentIndex, readTo);
        currentIndex = readTo;

        return lastSubList;
    }

    /**
     * Removes the last Sub List of results from the underlying List object
     * that we are reading from
     */
    public void remove() {
        if (lastSubList == null) {
            throw new IllegalStateException("Can only call remove after first calling next");
        }
        currentIndex -= lastSubList.size();
        // calling clear here will remove the items from the underlying list
        lastSubList.clear();
        lastSubList = null;
    }

    public static <T> SubListIterator<T> inMediumChunks(List<T> fullList) {
        return new SubListIterator<T>(fullList, CHUNK_MEDIUM);
    }
}
