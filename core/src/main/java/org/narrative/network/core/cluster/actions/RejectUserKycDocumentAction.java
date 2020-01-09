package org.narrative.network.core.cluster.actions;

import lombok.experimental.FieldNameConstants;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.service.api.KycService;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.shared.authentication.ClusterUserSession;
import org.narrative.network.shared.struts.NetworkResponses;

import static org.narrative.common.util.CoreUtils.*;

@FieldNameConstants
public class RejectUserKycDocumentAction extends ClusterAction {
    public static final String ACTION_NAME = "reject-user-kyc-document";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private UserKyc userKyc;
    private String note;

    @Override
    public void validate() {
        UserKyc userKyc = getUserKyc();
        if (!exists(userKyc)) {
            throw UnexpectedError.getRuntimeException("UserKyc should never be null!");
        }
        if (!userKyc.getKycStatus().isAwaitingMetadata()) {
            throw UnexpectedError.getRuntimeException("UserKyc is not eligible for document rejection " + userKyc.getOid());
        }
    }

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String execute() throws Exception {
        KycService kycService = StaticConfig.getBean(KycService.class);

        kycService.updateKycUserStatus(
                userKyc,
                UserKycStatus.REJECTED,
                UserKycEventType.USER_INFO_MISSING_FROM_DOCUMENT,
                ClusterUserSession.getClusterUserSession().getClusterRole().getDisplayNameResolved(),
                note
        );

        return NetworkResponses.redirectResponse();
    }

    public UserKyc getUserKyc() {
        return userKyc;
    }

    public void setUserKyc(UserKyc userKyc) {
        this.userKyc = userKyc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
