package org.narrative.common.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 7, 2006
 * Time: 10:51:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class DAOObjectPair<T extends DAOObject, T2 extends DAOObject> {
    private final T objectOne;
    private final T2 objectTwo;

    public DAOObjectPair(T objectOne, T2 objectTwo) {
        this.objectOne = objectOne;
        this.objectTwo = objectTwo;
    }

    public T getObjectOne() {
        return objectOne;
    }

    public T2 getObjectTwo() {
        return objectTwo;
    }

}
