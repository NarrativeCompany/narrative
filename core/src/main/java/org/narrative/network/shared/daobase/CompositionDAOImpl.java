package org.narrative.network.shared.daobase;

import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 18, 2005
 * Time: 9:46:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompositionDAOImpl<T extends DAOObject, ID extends Serializable> extends NetworkDAOImpl<T, ID> {
    public CompositionDAOImpl(@NotNull Class<T> cls) {
        super(PartitionType.COMPOSITION, cls);
    }
}
