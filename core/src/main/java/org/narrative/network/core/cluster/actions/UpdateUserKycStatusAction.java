package org.narrative.network.core.cluster.actions;

import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.narrative.common.util.NameForDisplayProvider;
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
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

@FieldNameConstants
public class UpdateUserKycStatusAction extends ClusterAction {
    public static final String ACTION_NAME = "update-user-kyc-status";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    /**
     * Available options for reviewers
     */
    @ToString
    public enum KycStatusUpdateOption implements NameForDisplayProvider {
        APPROVED(UserKycStatus.APPROVED, UserKycEventType.APPROVED),
        DOCUMENT_SUSPICIOUS(UserKycStatus.REJECTED, UserKycEventType.DOCUMENT_SUSPICIOUS),
        SELFIE_PAPER_MISSING(UserKycStatus.REJECTED, UserKycEventType.SELFIE_PAPER_MISSING),
        SELFIE_LOW_QUALITY(UserKycStatus.REJECTED, UserKycEventType.SELFIE_LOW_QUALITY),
        SELFIE_MISMATCH(UserKycStatus.REJECTED, UserKycEventType.SELFIE_MISMATCH),
        ;

        private final UserKycStatus userKycStatus;
        private final UserKycEventType userKycEventType;

        KycStatusUpdateOption(UserKycStatus userKycStatus, UserKycEventType userKycEventType) {
            this.userKycStatus = userKycStatus;
            this.userKycEventType = userKycEventType;
        }

        @Override
        public String getNameForDisplay() {
            return  wordlet("kycStatusUpdateOption." + this.name());
        }

        public UserKycStatus getUserKycStatus() {
            return userKycStatus;
        }

        public UserKycEventType getUserKycEventType() {
            return userKycEventType;
        }
    }

    private UserKyc userKyc;
    private KycStatusUpdateOption newStatusOption;
    private String note;

    @Override
    public void checkRightAfterParams() {
        super.checkRightAfterParams();

        UserKyc userKyc = getUserKyc();
        if (userKyc == null) {
            throw UnexpectedError.getRuntimeException("UserKyc should never be null!");
        }
        if (!userKyc.getKycStatus().isEligibleForManualChange()) {
            throw UnexpectedError.getRuntimeException("UserKyc is not eligible for a manual change " + userKyc.getOid());
        }
        if (!newStatusOption.userKycStatus.isManualChangeStatus()) {
            throw UnexpectedError.getRuntimeException("New KYC status is not a valid manual change status " + newStatusOption);
        }
    }

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String execute() throws Exception {
        if (!exists(userKyc)) {
            throw UnexpectedError.getRuntimeException("Should always have a userKyc at this point!");
        }

        UserKycStatus originalStatus = userKyc.getKycStatus();

        if (originalStatus != newStatusOption.userKycStatus) {
            KycService kycService = StaticConfig.getBean(KycService.class);

            kycService.updateKycUserStatus(
                    userKyc,
                    newStatusOption.userKycStatus,
                    newStatusOption.userKycEventType,
                    ClusterUserSession.getClusterUserSession().getClusterRole().getDisplayNameResolved(),
                    note
            );
        }

        return NetworkResponses.redirectResponse();
    }

    public UserKyc getUserKyc() {
        return userKyc;
    }

    public void setUserKyc(UserKyc userKyc) {
        this.userKyc = userKyc;
    }

    public KycStatusUpdateOption getNewStatusOption() {
        return newStatusOption;
    }

    public void setNewStatusOption(KycStatusUpdateOption newStatusOption) {
        this.newStatusOption = newStatusOption;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
