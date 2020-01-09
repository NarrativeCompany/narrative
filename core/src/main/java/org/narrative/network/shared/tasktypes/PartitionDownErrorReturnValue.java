package org.narrative.network.shared.tasktypes;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 26, 2006
 * Time: 11:04:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class PartitionDownErrorReturnValue<T> {
    private final boolean handled;
    private final T returnValue;

    public PartitionDownErrorReturnValue(boolean handled) {
        this(handled, null);
    }

    public PartitionDownErrorReturnValue(boolean handled, T returnValue) {
        this.handled = handled;
        this.returnValue = returnValue;
    }

    public boolean isHandled() {
        return handled;
    }

    public T getReturnValue() {
        return returnValue;
    }

}
