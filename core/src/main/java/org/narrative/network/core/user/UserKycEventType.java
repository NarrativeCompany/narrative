package org.narrative.network.core.user;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Types for KYC events associated with a key identifying (optional) email content to be sent when the event type
 * is generated.
 */
public enum UserKycEventType implements IntegerEnum, NameForDisplayProvider {
    SUBMITTED(0, null),
    APPROVED(1, UserKycStatus.APPROVED),
    REJECTED(2, null),
    USER_INFO_MISSING_FROM_DOCUMENT(4, UserKycStatus.REJECTED),
    SELFIE_NOT_VALID(5, UserKycStatus.REJECTED),
    NOTE(7, null),
    REJECTED_DUPLICATE(8, UserKycStatus.REJECTED),
    KYC_CHECK_CLEAR_OR_CONSIDER(9, null),
    REVOKED(10, UserKycStatus.REVOKED),
    DOCUMENT_INVALID(11, UserKycStatus.REJECTED),
    PAID(12, null),
    REFUNDED(13, null),
    DOCUMENT_SUSPICIOUS(14, UserKycStatus.REJECTED),
    SELFIE_PAPER_MISSING(15, UserKycStatus.REJECTED),
    SELFIE_LOW_QUALITY(16, UserKycStatus.REJECTED),
    SELFIE_MISMATCH(17, UserKycStatus.REJECTED),
    USER_UNDERAGE(18, UserKycStatus.REJECTED),
    DOCUMENT_METADATA_ENTERED(19, null),
    ;

    private final int id;
    private final UserKycStatus sendEmailForStatus;

    UserKycEventType(int id, UserKycStatus sendEmailForStatus) {
        this.id = id;
        this.sendEmailForStatus = sendEmailForStatus;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isNote() {
        return this == NOTE;
    }

    public UserKycStatus getSendEmailForStatus() {
        return sendEmailForStatus;
    }

    public boolean isRevokedEventType() {
        return REVOKED.equals(this);
    }

    public boolean isApprovedEventType() {
        return APPROVED.equals(this);
    }

    public boolean isRefunded() {
        return REFUNDED.equals(this);
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("userKycEventType." + name());
    }
}