package org.narrative.common.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Apr 27, 2006
 * Time: 1:08:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class NameValuePair<T> {
    private final String name;
    private final T value;

    public NameValuePair(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
