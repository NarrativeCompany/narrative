package org.narrative.network.shared.services;

import org.narrative.network.shared.util.NetworkLogger;
import org.apache.catalina.tribes.Member;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Aug 29, 2006
 * Time: 9:26:26 AM
 */
public enum ServerRegistry {
    INSTANCE;

    private static final NetworkLogger logger = new NetworkLogger(ServerRegistry.class);
    private static final Map<String, String> registry = new HashMap<String, String>();
    public static final byte[] STATIC_UNIQUE_ID = {1, 1, 1, 1};

    public void addServer(String servletName, byte[] uniqueIdBytes) {
        String uniqueId = getUniqueIdForIdBytes(uniqueIdBytes);
        if (logger.isInfoEnabled()) {
            logger.info("Adding server id/" + uniqueId + " -> " + servletName);
        }
        registry.put(uniqueId, servletName);
    }

    public void removeMember(Member member) {
        String uniqueId = getUniqueIdForMember(member);
        if (logger.isInfoEnabled()) {
            logger.info("Removing server id/" + uniqueId + " with servletName/" + registry.get(uniqueId) + " memberName/" + member.getName());
        }
        registry.remove(uniqueId);
    }

    public String getNameForMember(Member member) {
        return registry.get(getUniqueIdForMember(member));
    }

    private String getUniqueIdForMember(Member member) {
        return getUniqueIdForIdBytes(member.getUniqueId());
    }

    private String getUniqueIdForIdBytes(byte[] uniqueIdBytes) {
        return Arrays.toString(uniqueIdBytes);
    }

    public Collection<String> getServerNames() {
        return new TreeSet<String>(registry.values());
    }
}