package org.narrative.network.shared.jsptags;

import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.AuthZoneMaster;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.interceptors.UserSessionRequestType;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class StaticImageProperties {
    private final String id;
    private final String relativeFilePath;
    private final int width;
    private final int height;
    private final String defaultAltTitleWordletKey;
    private final String forceCssClass;

    public StaticImageProperties(String id, String defaultFilePath, int width, int height, String defaultAltTitleWordletKey, String forceCssClass) {
        this.id = id;
        this.relativeFilePath = defaultFilePath;
        this.width = width;
        this.height = height;
        this.defaultAltTitleWordletKey = defaultAltTitleWordletKey;
        this.forceCssClass = forceCssClass;
    }

    public String getId() {
        return id;
    }

    // todo:jw:remove: this is necessary for the debug main in StaticImageTag, keeping for now.
    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    public String getDefaultSecureFileUrl() {
        return getFileUrl(true);
    }

    public String getDefaultFileUrl() {
        return getFileUrl(false);
    }

    private String getFileUrl(boolean isSecureUrl) {
        String baseUrl;
        if (isNetworkContextSet()) {
            UserSessionRequestType userSessionRequestType = networkContext().getRequestType().getUserSessionRequestType();

            // bl: cluster CPs need to use absolute URLs now. can continue to use relative URLs for Cluster and Translations.
            if (userSessionRequestType!=null && !userSessionRequestType.isClusterCp() && !networkContext().isProcessingJspEmail()) {
                baseUrl = NetworkRegistry.getInstance().getStaticPath();
            } else {
                AuthZoneMaster authZoneMaster = NetworkMailUtil.getEmailFromAuthZoneMaster();
                if (authZoneMaster != null) {
                    baseUrl = (isSecureUrl ? authZoneMaster.getSecureBaseUrl() : authZoneMaster.getBaseUrl()) + NetworkRegistry.getInstance().getStaticPath();
                } else {
                    AuthRealm authRealm = networkContext().getAuthRealm();
                    assert !isSecureUrl || authRealm.isSslEnabled() : "Should never get secure static base URL when SSL isn't enabled on the authRealm! authRealm/" + authRealm.getClass().getSimpleName() + " authZone/" + networkContext().getAuthZone();
                    baseUrl = authRealm.getStaticBaseUrl();
                }
            }
        } else {
            baseUrl = NetworkRegistry.getInstance().getStaticPath();
        }
        return baseUrl + relativeFilePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDefaultAltTitleWordletKey() {
        return defaultAltTitleWordletKey;
    }

    public String getAltTitle() {
        if (isEmpty(getDefaultAltTitleWordletKey())) {
            return null;
        }
        return wordlet(getDefaultAltTitleWordletKey());
    }

    public String getForceCssClass() {
        return forceCssClass;
    }

}