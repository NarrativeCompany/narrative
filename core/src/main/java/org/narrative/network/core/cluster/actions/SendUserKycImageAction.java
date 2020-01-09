package org.narrative.network.core.cluster.actions;

import lombok.experimental.FieldNameConstants;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.KycImageType;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.customizations.narrative.services.GoogleCloudStorageFileHandler;
import org.narrative.network.shared.struts.NetworkResponses;

import javax.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.narrative.common.util.CoreUtils.*;

@FieldNameConstants
public class SendUserKycImageAction extends ClusterAction {
    public static final String ACTION_NAME = "kyc-image";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private UserKyc userKyc;
    private KycImageType imageType;
    private int submission = 1;

    private String contentType;
    private InputStream inputStream;
    private String contentDisposition = IPHttpUtil.CONTENT_DISPOSITION_INLINE;

    @Override
    public void checkRightAfterParams() {
        UserKyc userKyc = getUserKyc();
        if (!exists(userKyc)) {
            throw UnexpectedError.getRuntimeException("UserKyc should never be null!");
        }
        if (imageType==null) {
            throw UnexpectedError.getRuntimeException("imageType is required!");
        }
    }

    @Override
    public String input() throws Exception {
        ObjectPair<String, File> pair = GoogleCloudStorageFileHandler.KYC_FILES.getFile(userKyc.getNetworkPath(imageType, submission));
        if(pair==null) {
            // file not found
            getNetworkContext().getReqResp().setStatus(HttpServletResponse.SC_NOT_FOUND);
            return NetworkResponses.emptyResponse();
        }
        contentType = pair.getOne();
        File encryptedFile = pair.getTwo();
        inputStream = userKyc.getUser().getAuthZone().decryptingInputStream(new BufferedInputStream(new FileInputStream(encryptedFile)), userKyc.getEncryptionSalt());
        return INPUT;
    }

    public UserKyc getUserKyc() {
        return userKyc;
    }

    public void setUserKyc(UserKyc userKyc) {
        this.userKyc = userKyc;
    }

    public KycImageType getImageType() {
        return imageType;
    }

    public void setImageType(KycImageType imageType) {
        this.imageType = imageType;
    }

    public int getSubmission() {
        return submission;
    }

    public void setSubmission(int submission) {
        this.submission = submission;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }
}
