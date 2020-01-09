package org.narrative.network.core.user;

import org.narrative.network.customizations.narrative.service.api.KycService;

/**
 * Date: 9/9/19
 * Time: 8:31 AM
 *
 * @author brian
 */
public enum KycImageType {
    DOCUMENT_FRONT("doc-front", KycService.DOC_FRONT_IMAGE),
    DOCUMENT_BACK("doc-back", KycService.DOC_BACK_IMAGE),
    SELFIE("selfie", KycService.LIVE_PHOTO_IMAGE)
    ;

    private final String filenameBase;
    private final String paramName;

    KycImageType(String filenameBase, String paramName) {
        this.filenameBase = filenameBase;
        this.paramName = paramName;
    }

    public String getFilenameBase() {
        return filenameBase;
    }

    public String getParamName() {
        return paramName;
    }

    public String getFieldNameWordletKey() {
        return "kycImageType." + this;
    }
}
