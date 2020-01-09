package org.narrative.network.shared.tasktypes;

import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.cluster.partition.Partition;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 21, 2006
 * Time: 8:38:04 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CompositionTaskImpl<T> extends GlobalTaskImpl<T> {
    private Partition partition;

    protected CompositionTaskImpl() {}

    protected CompositionTaskImpl(boolean isForceWritable) {
        super(isForceWritable);
    }

    protected CompositionTaskImpl(ValidationHandler validationHandler) {
        super(validationHandler);
    }

    public Partition getPartition() {
        return partition;
    }

    void setPartition(Partition partition) {
        this.partition = partition;
    }

    protected GSession getCompositionSession() {
        return partition.getPartitionType().currentSession();
    }
}
