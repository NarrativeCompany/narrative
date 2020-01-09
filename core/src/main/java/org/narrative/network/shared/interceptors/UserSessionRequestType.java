package org.narrative.network.shared.interceptors;

import org.narrative.network.shared.authentication.ClusterUserSession;
import org.narrative.network.shared.authentication.UserSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Aug 1, 2007
 * Time: 10:08:23 AM
 *
 * @author brian
 */
public enum UserSessionRequestType {
    CLUSTER_CP(ClusterUserSession.class) {
        @Override
        public String getSessionKeySuffix() {
            return "cluster-cp";
        }
    };

    private final Class<? extends UserSession> userSessionClass;

    UserSessionRequestType(Class<? extends UserSession> userSessionClass) {
        this.userSessionClass = userSessionClass;
    }

    private static final Map<Class<? extends UserSession>, UserSessionRequestType> CLASS_TO_REQUEST_TYPE;

    static {
        Map<Class<? extends UserSession>, UserSessionRequestType> map = new HashMap<>();
        for (UserSessionRequestType userSessionRequestType : values()) {
            if (userSessionRequestType.userSessionClass != null) {
                map.put(userSessionRequestType.userSessionClass, userSessionRequestType);
            }
        }
        CLASS_TO_REQUEST_TYPE = Collections.unmodifiableMap(map);
    }

    public abstract String getSessionKeySuffix();

    public Class<? extends UserSession> getUserSessionClass() {
        return userSessionClass;
    }

    public boolean isClusterCp() {
        return this == CLUSTER_CP;
    }

    public static UserSessionRequestType getRequestTypeFromSession(UserSession userSession) {
        if (userSession == null) {
            return null;
        }
        UserSessionRequestType ret = CLASS_TO_REQUEST_TYPE.get(userSession.getClass());
        assert ret != null : "Should always find a UserSessionRequestType for UserSession! class/" + userSession.getClass().getName();
        return ret;
    }
}
