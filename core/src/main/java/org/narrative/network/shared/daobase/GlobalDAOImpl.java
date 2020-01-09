package org.narrative.network.shared.daobase;

import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 18, 2005
 * Time: 9:14:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class GlobalDAOImpl<T extends DAOObject, ID extends Serializable> extends NetworkDAOImpl<T, ID> {
    public GlobalDAOImpl(@NotNull Class<T> cls) {
        super(PartitionType.GLOBAL, cls);
    }
}
