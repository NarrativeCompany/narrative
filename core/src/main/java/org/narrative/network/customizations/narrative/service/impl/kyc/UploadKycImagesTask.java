package org.narrative.network.customizations.narrative.service.impl.kyc;

import org.narrative.common.util.IPIOUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.common.util.images.ImageProperties;
import org.narrative.common.util.images.ImageUtils;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.KycImageType;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.customizations.narrative.service.api.model.kyc.KycIdentificationType;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/9/19
 * Time: 8:56 AM
 *
 * @author brian
 */
public class UploadKycImagesTask extends AreaTaskImpl<Object> {

    private final UserKyc userKyc;
    private final Map<KycImageType,File> images = new LinkedHashMap<>();

    private final Map<KycImageType,ImageProperties> imageProperties = new HashMap<>();

    public UploadKycImagesTask(UserKyc userKyc, KycIdentificationType kycIdentificationType, File livePhotoFile, File documentFrontFile, File documentBackFile) {
        this.userKyc = userKyc;
        images.put(KycImageType.DOCUMENT_FRONT, documentFrontFile);
        if(kycIdentificationType.isRequiresBackImage()) {
            images.put(KycImageType.DOCUMENT_BACK, documentBackFile);
        }
        images.put(KycImageType.SELFIE, livePhotoFile);
    }

    @Override
    protected void validate(ValidationHandler handler) {
        if (!userKyc.isEligibleForSubmission()) {
            throw UnexpectedError.getRuntimeException("Attempting to submit user for KYC verification when in the wrong state!  User: " + userKyc.getOid());
        }

        for (Map.Entry<KycImageType, File> entry : images.entrySet()) {
            KycImageType imageType = entry.getKey();
            File imageFile = entry.getValue();

            if(handler.validateNotNull(imageFile, imageType.getParamName(), imageType.getFieldNameWordletKey())) {
                // bl: if we can get ImageProperties, then that indicates it is a valid image file.
                ImageProperties properties = ImageUtils.getImageProperties(imageFile);
                if(properties==null) {
                    handler.addWordletizedInvalidFieldError(imageType.getParamName(), imageType.getFieldNameWordletKey());
                } else {
                    imageProperties.put(imageType, properties);
                }
            }
        }
    }

    @Override
    protected Object doMonitoredTask() {
        AuthZone authZone = userKyc.getUser().getAuthZone();

        // bl: generate an encryption salt for these users for encrypting the files
        if (userKyc.getEncryptionSalt()==null) {
            userKyc.setEncryptionSalt(authZone.generateEncryptionSalt());
            userKyc.setSubmissionCount(1);
        } else {
            userKyc.setSubmissionCount(userKyc.getSubmissionCount()+1);
        }

        for (Map.Entry<KycImageType, File> entry : images.entrySet()) {
            KycImageType imageType = entry.getKey();
            File imageFile = entry.getValue();

            File encryptedFile = createTempFile(userKyc.getOid() + userKyc.getFilenameBase(imageType), "encrypted", false, true);

            try {
                OutputStream os = authZone.encryptingOutputStream(new BufferedOutputStream(new FileOutputStream(encryptedFile)), userKyc.getEncryptionSalt());
                IPIOUtil.doStreamFileToOut(imageFile, os, true);
            } catch (IOException e) {
                throw UnexpectedError.getRuntimeException("Failed creating encrypted file!", e);
            }

            ImageProperties properties = imageProperties.get(imageType);

            GoogleCloudStorageFileHandler.KYC_FILES.putFile(userKyc.getNetworkPath(imageType), encryptedFile, properties.getFormat().getMimeType());
        }

        return null;
    }
}
