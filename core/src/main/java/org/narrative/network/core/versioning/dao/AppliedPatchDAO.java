package org.narrative.network.core.versioning.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.versioning.AppliedPatch;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 20, 2006
 * Time: 3:30:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppliedPatchDAO extends GlobalDAOImpl<AppliedPatch, OID> {
    public AppliedPatchDAO() {
        super(AppliedPatch.class);
    }

    public Map<Partition, Map<String, AppliedPatch>> getAllPartitionToPatchNameToAppliedPatches() {
        Map<Partition, Map<String, AppliedPatch>> ret = new HashMap<Partition, Map<String, AppliedPatch>>();
        Collection<AppliedPatch> allAppliedPatches = getAll();
        for (AppliedPatch appliedPatch : allAppliedPatches) {
            Partition partition = appliedPatch.getPartition();
            Map<String, AppliedPatch> patchNameToAppliedPatchForPartition = ret.get(partition);
            if (patchNameToAppliedPatchForPartition == null) {
                ret.put(partition, patchNameToAppliedPatchForPartition = new HashMap<String, AppliedPatch>());
            }
            patchNameToAppliedPatchForPartition.put(appliedPatch.getName(), appliedPatch);
        }
        return ret;
    }

    public AppliedPatch getByName(String patchName, Partition partition) {
        return (AppliedPatch) getGSession().getNamedQuery("appliedPatch.getPatchByName").setParameter("name", patchName).setParameter("partition", partition).uniqueResult();
    }
}
