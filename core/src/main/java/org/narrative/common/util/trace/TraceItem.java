package org.narrative.common.util.trace;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Oct 31, 2006
 * Time: 1:22:38 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TraceItem {
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();

    {
        numberFormat.setMaximumFractionDigits(2);
    }

    private long startTime;
    private long endTime;
    private final List<TraceItem> children = new LinkedList<TraceItem>();
    private TraceItem parent;

    protected TraceItem() {
        this.startTime = System.nanoTime();
    }

    public abstract String getDisplayName();

    public void endTrace() {
        if (endTime == 0) {
            this.endTime = System.nanoTime();
        }

    }

    public List<TraceItem> getHierarchyAsList() {
        List<TraceItem> list = new LinkedList<TraceItem>();
        getHierarchyAsList(list);
        return list;
    }

    private void getHierarchyAsList(List<TraceItem> list) {
        list.add(this);
        for (TraceItem child : getChildren()) {
            child.getHierarchyAsList(list);
        }
    }

    public int getCurDepth() {
        int depth = 0;
        TraceItem root = this;
        while (root.getParent() != null) {
            root = root.getParent();
            depth++;
        }
        return depth;
    }

    public int getMaxDepth() {
        return getMaxDepth(this, 0);
    }

    private int getMaxDepth(TraceItem parent, int depth) {
        depth++;
        int maxDepth = depth;
        for (TraceItem child : parent.getChildren()) {
            int childDepth = child.getMaxDepth(child, depth);
            if (childDepth > maxDepth) {
                maxDepth = childDepth;
            }
        }
        return maxDepth;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public double getDurationMS() {
        return ((double) getDuration() / 1000000);

    }

    public String getDurationMSFormatted() {
        return numberFormat.format(getDurationMS());
    }

    public List<TraceItem> getChildren() {
        return children;
    }

    void addChild(TraceItem traceItem) {
        children.add(traceItem);
        traceItem.setParent(this);
    }

    public TraceItem getParent() {
        return parent;
    }

    private void setParent(TraceItem parent) {
        this.parent = parent;
    }

    public TraceItem getRoot() {
        TraceItem root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    public double getTotalDurationRatio() {
        return getRoot().getDuration() != 0 ? (double) getDuration() / (double) getRoot().getDuration() : 0;
    }

    public String getTotalDurationRatioFormatted() {
        return NumberFormat.getPercentInstance().format(getTotalDurationRatio());
    }

    public double getParentDurationRatio() {
        if (getParent() == null) {
            return 1;
        }
        return getParent().getDuration() != 0 ? (double) getDuration() / (double) getParent().getDuration() : 0;
    }

    public String getParentDurationRatioFormatted() {
        return NumberFormat.getPercentInstance().format(getParentDurationRatio());
    }

}
