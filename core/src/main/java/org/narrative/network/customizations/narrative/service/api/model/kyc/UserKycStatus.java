package org.narrative.network.customizations.narrative.service.api.model.kyc;

import com.google.common.collect.Sets;
import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;

import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public enum UserKycStatus implements IntegerEnum, NameForDisplayProvider {
    /**
     * Uninitialized
     */
    NONE(0),
    /**
     * Ready to submit - i.e. paid
     */
    READY_FOR_VERIFICATION(1),
    /**
     * Waiting for manual document metadata extraction
     */
    AWAITING_METADATA(2),
    /**
     * Waiting for manual review
     */
    IN_REVIEW(3),
    /**
     * Approved - certified
     */
    APPROVED(4),
    /**
     * Submission rejected - not certified
     */
    REJECTED(5),
    /**
     * User KYC status has been revoked for this user
     */
    REVOKED(6);

    /**
     * States indicating a user's KYC certification can be paid/started
     */
    private static final Set<UserKycStatus> USER_KYC_STARTABLE_STATUS_SET = Sets.immutableEnumSet(NONE, REJECTED);

    /**
     * States indicating a user's KYC status is eligible for a manual change
     */
    private static final Set<UserKycStatus> USER_KYC_MANUAL_CHANGE_ELIGIBLE_STATUS_SET = Sets.immutableEnumSet(IN_REVIEW);

    /**
     * States available for manual change
     */
    public static final Set<UserKycStatus> USER_KYC_MANUAL_CHANGE_STATUS_SET = Sets.immutableEnumSet(APPROVED, REJECTED);

    /**
     * States indicating the verification process is pending
     */
    public static final Set<UserKycStatus> USER_KYC_PENDING_STATUS_SET = Sets.immutableEnumSet(AWAITING_METADATA, IN_REVIEW);

    private final int id;

    UserKycStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isNone() {
        return NONE.equals(this);
    }

    public boolean isReadyForVerification() {
        return READY_FOR_VERIFICATION.equals(this);
    }

    public boolean isAwaitingMetadata() {
        return AWAITING_METADATA.equals(this);
    }

    public boolean isApproved() {
        return APPROVED.equals(this);
    }

    public boolean isRevoked() {
        return REVOKED.equals(this);
    }

    public boolean isRejected() {
        return REJECTED.equals(this);
    }

    public boolean isStartCheckEligible() {
        return USER_KYC_STARTABLE_STATUS_SET.contains(this);
    }

    public boolean isEligibleForManualChange() {
        return USER_KYC_MANUAL_CHANGE_ELIGIBLE_STATUS_SET.contains(this);
    }

    public boolean isManualChangeStatus() {
        return USER_KYC_MANUAL_CHANGE_STATUS_SET.contains(this);
    }

    public boolean isPendingStatus() {
        return USER_KYC_PENDING_STATUS_SET.contains(this);
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("userKycStatus." + name());
    }
}
