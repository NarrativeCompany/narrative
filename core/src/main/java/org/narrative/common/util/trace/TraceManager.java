package org.narrative.common.util.trace;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 31, 2006
 * Time: 1:21:37 PM
 */
public class TraceManager {

    private static final ThreadLocal<Boolean> trace = new ThreadLocal<Boolean>();
    private static final ThreadLocal<TraceItem> rootTraceItem = new ThreadLocal<TraceItem>();
    private static final ThreadLocal<LinkedList<TraceItem>> traceItemStack = new ThreadLocal<LinkedList<TraceItem>>();

    public static boolean isTracing() {
        return trace.get() != null && trace.get();
    }

    public static void setTracing(boolean isTracing) {
        trace.set(isTracing);
        if (!isTracing) {
            rootTraceItem.remove();
            traceItemStack.remove();
        }
    }

    public static TraceItem getRootTraceItem() {
        return rootTraceItem.get();
    }

    public static void endAllTraces() {
        endAllTraces(rootTraceItem.get());
    }

    private static void endAllTraces(TraceItem parent) {
        if (parent == null) {
            return;
        }
        parent.endTrace();
        for (TraceItem ti : parent.getChildren()) {
            endAllTraces(ti);
        }
    }

    public static TraceItem startTrace(TraceItem traceItem) {
        if (rootTraceItem.get() == null) {
            rootTraceItem.set(traceItem);
            traceItemStack.set(new LinkedList<TraceItem>());
        } else {
            TraceItem parent = traceItemStack.get().getLast();
            parent.addChild(traceItem);
        }
        traceItemStack.get().addLast(traceItem);
        return traceItem;
    }

    public static void endTrace(TraceItem traceItem) {
        assert traceItemStack.get() != null : "endTrace was called with no item in the stack";
        assert traceItem.equals(traceItemStack.get().getLast()) : "endTrace called with: " + traceItem.getDisplayName() + " but doesn't match the last item on the stack: " + traceItemStack.get().getLast().getDisplayName();
        TraceItem ti = traceItemStack.get().removeLast();
        ti.endTrace();
    }
}