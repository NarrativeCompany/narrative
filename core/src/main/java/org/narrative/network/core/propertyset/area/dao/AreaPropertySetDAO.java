package org.narrative.network.core.propertyset.area.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.LRUMap;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.narrative.network.shared.tasktypes.PartitionTask;
import org.narrative.network.shared.tasktypes.TaskIsolationLevel;
import org.narrative.network.shared.tasktypes.TaskOptions;

/**
 * Date: Dec 2, 2005
 * Time: 4:25:56 PM
 *
 * @author Brian
 */
public class AreaPropertySetDAO extends GlobalDAOImpl<AreaPropertySet, OID> {
    public AreaPropertySetDAO() {
        super(AreaPropertySet.class);
    }

    /**
     * todo: how big should this map be?  how many AreaPropertySet objects
     * do we want to keep in memory at a time?
     */
    private static final LRUMap<OID, AreaPropertySet> AREA_PROPERTY_SET_OID_TO_AREA_PROPERTY_SET = new LRUMap<OID, AreaPropertySet>();

    public static void invalidateAreaPropertySet(AreaPropertySet areaPropertySet) {
        AREA_PROPERTY_SET_OID_TO_AREA_PROPERTY_SET.remove(areaPropertySet.getOid());
    }

    /**
     * get an AreaPropertySet by OID.  does a Hibernate get instead of load
     * to ensure that a proxy is not returned.
     *
     * @param oid the OID to get an AreaPropertySet for
     * @return the AreaPropertySet for the specified OID, or null if one does not exist.
     */
    @Override
    public AreaPropertySet get(OID oid) {
        // bl: do a get instead of a load to ensure that the class is properly
        // instantiated and initialized.  otherwise, a proxy object will be returned
        // that will need to be initialized.
        return getReal(oid);
    }

    /**
     * get an AreaPropertySet by OID.  if the AreaPropertySet is meant to be read-only,
     * then readOnly can be supplied as true for an optimization to prevent
     * Hibernate from having to recreate an instance of the object each time.
     *
     * @param oid      the oid to get an AreaPropertySet for
     * @param readOnly true if the AreaPropertySet is being loaded for read-only purposes.
     *                 if true, the returned AreaPropertySet will not be associated with the current
     *                 session and should never be updated.  if false, the returned AreaPropertySet
     *                 will be associated with the current session and can be updated.
     * @return the AreaPropertySet for the given oid
     */
    public AreaPropertySet get(final OID oid, boolean readOnly) {
        // if not a read-only lookup, then just do a get from Hibernate so that
        // the AreaPropertySet that is returned is associated with the Hibernate session
        if (!readOnly) {
            return get(oid);
        }
        if (oid == null) {
            return null;
        }
        AreaPropertySet ret = AREA_PROPERTY_SET_OID_TO_AREA_PROPERTY_SET.get(oid);
        if (ret != null) {
            return ret;
        }
        // nb: do the lookup in a separate session so that when the object is
        // returned, it isn't associated with any sessions.
        ret = getPartitionType().doTask(getCurrentPartition(), new TaskOptions(TaskIsolationLevel.ISOLATED), new PartitionTask<AreaPropertySet>(false) {
            protected AreaPropertySet doMonitoredTask() {
                return get(oid);
            }
        });
        AREA_PROPERTY_SET_OID_TO_AREA_PROPERTY_SET.put(oid, ret);
        return ret;
    }
}
