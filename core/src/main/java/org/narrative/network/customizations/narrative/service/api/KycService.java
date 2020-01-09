package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.service.api.model.UserKycDTO;
import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.customizations.narrative.service.impl.kyc.DocCheckUserProps;

import javax.validation.constraints.NotNull;

import java.io.File;

/**
 * Know Your Customer related services.
 */
public interface KycService {
    String LIVE_PHOTO_IMAGE = "livePhotoImage";
    String DOC_FRONT_IMAGE = "docFrontImage";
    String DOC_BACK_IMAGE = "docBackImage";

    /**
     * Retrieve the KYC state for a user
     *
     * @return The user's KYC state
     */
    UserKycDTO getKycStateForUser(OID userOid);

    /**
     * Submit an applicant for KYC verification
     *
     * @param user                  The user requesting verification
     * @param kycIdentificationType The identification type provided as #documentFiles
     * @param livePhotoFile         The live photo file to use for this check
     * @param documentFrontFile     The image scan of the relevant document front for the #kycIdentificationType
     * @param documentBackFile      The image scan of the relevant document back for the #kycIdentificationType (optional depending on document type)
     * @return {@link UserKyc} for which the KYC submission was performed
     */
    UserKycDTO submitKycApplicant(User user, KycIdentificationType kycIdentificationType, @NotNull File livePhotoFile, @NotNull File documentFrontFile, File documentBackFile);

    void updateKycData(User user, DocCheckUserProps props, String actorDisplayName);

    /**
     * Update a {@link org.narrative.network.core.user.UserKyc) with a new status
     *
     * @param userKyc The {@link UserKyc} of interest
     * @param newUserKycStatus The new status
     * @param eventType The event type to generate for this status change
     * @param actorDisplayName The actor setting this status (optional)
     * @param note Note for the update (optional)
     */
    void updateKycUserStatus(@NotNull UserKyc userKyc, @NotNull UserKycStatus newUserKycStatus, UserKycEventType eventType, String actorDisplayName, String note);
}
