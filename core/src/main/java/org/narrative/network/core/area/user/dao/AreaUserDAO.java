package org.narrative.network.core.area.user.dao;

import org.narrative.common.cache.Cache;
import org.narrative.common.cache.CacheManager;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.AreaNotificationType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 13, 2005
 * Time: 11:59:37 PM
 */
public class AreaUserDAO extends GlobalDAOImpl<AreaUser, OID> {
    public AreaUserDAO() {
        super(AreaUser.class);
    }

    /**
     * the cache in which to keep {userOid,areaOid} -> areaUserOid lookups
     */
    private static Cache areaUserOidCache = null;
    /**
     * null oid for caching
     */
    private static final OID NULL_OID = new OID(0);

    public static void init() {
        areaUserOidCache = CacheManager.getCache(AreaUserDAO.class.getName() + "AreaUserOidCache");
    }

    public static void invalidateAreaUserCache(final OID areaOid, final OID userOid) {
        //also do it now so the current session doesn't have stale state, in case an area user was added but not yet committed
        areaUserOidCache.remove(new ObjectPair<OID, OID>(areaOid, userOid));
    }

    public AreaUser getAreaUserFromUserAndArea(OID userOID, OID areaOID) {
        OID areaUserOid = getAreaUserOidFromUserAndAreaCache(userOID, areaOID);
        if (areaUserOid != null) {
            if (areaUserOid == NULL_OID) {
                return null;
            }
            return get(areaUserOid);
        }
        AreaUser ret = getAreaUserFromUserAndAreaNoCache(userOID, areaOID);
        areaUserOid = exists(ret) ? ret.getOid() : NULL_OID;
        areaUserOidCache.put(new ObjectPair<OID, OID>(areaOID, userOID), areaUserOid);
        return ret;
    }

    public OID getAreaUserOidFromUserAndAreaCache(OID userOID, OID areaOID) {
        return (OID) areaUserOidCache.get(new ObjectPair<OID, OID>(areaOID, userOID));
    }

    private AreaUser getAreaUserFromUserAndAreaNoCache(OID userOID, OID areaOID) {
        return (AreaUser) getGSession().getNamedQuery("areaUser.getFromUserOIDAreaOID").setParameter("userOID", userOID).setParameter("areaOID", areaOID).uniqueResult();
    }

    public int getMemberCountForArea(Area area) {
        return ((Number) getGSession().getNamedQuery("areaUser.getMemberCountForArea").setParameter("area", area).uniqueResult()).intValue();
    }

    public List<User> getAllUsersWithCommunitySubscription(Collection<OID> userOids, AreaNotificationType notificationType) {
        if (isEmptyOrNull(userOids)) {
            return Collections.emptyList();
        }

        List<User> results = newLinkedList();
        SubListIterator<OID> userOidChunks = newSubListIterator(userOids, SubListIterator.CHUNK_LARGE);
        while (userOidChunks.hasNext()) {
            results.addAll(getGSession().getNamedQuery("areaUser.getAllUsersWithCommunitySubscription").setParameter("notificationTypeBitmask", notificationType.getBitmask()).setParameterList("userOids", userOidChunks.next()).list());
        }

        return results;
    }

    public List<OID> getAllUserOidsWithCommunitySubscription(Collection<OID> userOids, AreaNotificationType notificationType) {
        if (isEmptyOrNull(userOids)) {
            return Collections.emptyList();
        }

        List<OID> results = newLinkedList();
        SubListIterator<OID> userOidChunks = newSubListIterator(userOids, SubListIterator.CHUNK_LARGE);
        while (userOidChunks.hasNext()) {
            results.addAll(getGSession().getNamedQuery("areaUser.getAllUserOidsWithCommunitySubscription").setParameter("notificationTypeBitmask", notificationType.getBitmask()).setParameterList("userOids", userOidChunks.next()).list());
        }

        return results;
    }

    public Map<OID, OID> getAreaUserOidToUserOidMap(Collection<OID> areaUserOids) {
        if (isEmptyOrNull(areaUserOids)) {
            return Collections.emptyMap();
        }
        List<ObjectPair<OID, OID>> pairs = getGSession().getNamedQuery("areaUser.getAreaUserOidAndUserOid").setParameterList("areaUserOids", areaUserOids).list();
        return ObjectPair.getAsMap(pairs);
    }

    public List<OID> getUserOidsForAreaUserOids(List<OID> areaUserOids) {
        if (isEmptyOrNull(areaUserOids)) {
            return newLinkedList();
        }
        List<OID> ret = newLinkedList();
        SubListIterator<OID> iter = new SubListIterator<OID>(areaUserOids, SubListIterator.CHUNK_LARGE);
        while (iter.hasNext()) {
            List<OID> areaUserOidChunk = iter.next();
            ret.addAll(getGSession().getNamedQuery("areaUser.getUserOidsForAreaUserOids").setParameterList("areaUserOids", areaUserOidChunk).list());
        }
        return ret;
    }

    public long getActiveMemberCount(Area area, Timestamp cutoff) {
        return ((Number) getGSession().getNamedQuery("areaUser.getActiveMemberCount").setParameter("area", area).setParameter("cutoff", cutoff).uniqueResult()).longValue();
    }

    public List<AreaUser> getAllUsersForAreaCircle(AreaCircle areaCircle) {
        return getGSession().createNamedQuery("areaUser.getAllUsersForAreaCircle", AreaUser.class)
                .setParameter("area", areaCircle.getArea())
                .setParameter("areaCircle", areaCircle)
                .list();
    }
}
