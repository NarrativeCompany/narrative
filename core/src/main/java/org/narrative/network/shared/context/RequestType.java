package org.narrative.network.shared.context;

import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.ClusterCpAuthRealm;
import org.narrative.network.core.user.KycQueueAuthRealm;
import org.narrative.network.shared.interceptors.UserSessionRequestType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 18, 2005
 * Time: 2:28:32 AM
 */
public enum RequestType {
    CLUSTER_CP(UserSessionRequestType.CLUSTER_CP, ClusterCpAuthRealm.INSTANCE),
    KYC_QUEUE(UserSessionRequestType.CLUSTER_CP, KycQueueAuthRealm.INSTANCE),
    NARRATIVE(null, null);

    private String baseUrl;
    private final UserSessionRequestType userSessionRequestType;
    private final AuthRealm singletonAuthRealm;

    RequestType(UserSessionRequestType userSessionRequestType, AuthRealm singletonAuthRealm) {
        this.userSessionRequestType = userSessionRequestType;
        this.singletonAuthRealm = singletonAuthRealm;
    }

    public static void init() {
        NetworkRegistry registry = NetworkRegistry.getInstance();
        RequestType.CLUSTER_CP.setBaseUrl(registry.getClusterCpUrl());
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UserSessionRequestType getUserSessionRequestType() {
        return userSessionRequestType;
    }

    public boolean isClusterCp() {
        return this == CLUSTER_CP;
    }

    public boolean isKycQueue() {
        return this == KYC_QUEUE;
    }

    public AuthRealm getSingletonAuthRealm() {
        assert singletonAuthRealm != null : "Should never get the singletonAuthRealm for RequestTypes that aren't singleton AuthRealms! type/" + this;
        return singletonAuthRealm;
    }

    public boolean isRedirectUrlAllowed(String redirectUrl) {
        if (isEmpty(redirectUrl)) {
            return false;
        }
        // bl: blindly trust KYC queue redirect URLs since there aren't any security concerns here.
        // no need to configure the actual domain name in the app.
        if (isKycQueue()) {
            return true;
        }
        return redirectUrl.startsWith(getBaseUrl());

    }
}
