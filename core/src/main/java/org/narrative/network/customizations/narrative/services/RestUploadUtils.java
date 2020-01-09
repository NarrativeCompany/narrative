package org.narrative.network.customizations.narrative.services;

import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.content.base.UploadedFileData;
import org.narrative.network.core.fileondisk.base.FileType;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-01-11
 * Time: 08:45
 *
 * @author jonmark
 */
public class RestUploadUtils {
    private static final NetworkLogger logger = new NetworkLogger(RestUploadUtils.class);

    private RestUploadUtils() {
        throw UnexpectedError.getRuntimeException("Should never contruct this utility class!");
    }

    public static File getUploadedFile(MultipartFile file, boolean keepOriginalExtension) {
        // bl: first store the file locally so that we can do image processing
        String filename = file.getOriginalFilename();
        File localFile = createTempFile(
                IPStringUtil.getStringBeforeLastIndexOf(filename, "."),
                keepOriginalExtension ? IPStringUtil.getStringAfterLastIndexOf(filename, ".") : "tmp",
                true,
                true);
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed transferring uploaded file to temp file at " + localFile.getAbsolutePath(), e);
        }

        return localFile;
    }

    public static UploadedFileData getUploadedFileData(MultipartFile file, FileUsageType fileUsageType, boolean keepOriginalExtension) {
        assert fileUsageType != null : "A fileUsageType must be provided!";

        File localFile = getUploadedFile(file, keepOriginalExtension);

        // create the UploadedFileData
        return UploadedFileData.getNewUploadedFileData(OIDGenerator.getNextOID(), fileUsageType, localFile, file.getContentType(), file.getOriginalFilename());
    }

    public static UploadedFileData getUploadedFileData(MultipartFile file, FileUsageType fileUsageType) {
        return getUploadedFileData(file, fileUsageType, false);
    }

    public static void validateUploadedFileData(NetworkContext networkContext, UploadedFileData uploadedFileData, FileType forcedFileType, ValidationContext validationContext) {
        FileType fileType = uploadedFileData.getFileType();
        uploadedFileData.setProperType(uploadedFileData.getFileUsageType().isAllowFileTypeForCurrentRole(fileType));

        // jw: in some cases (liked attachments), the FileUsageType supports more types than we want to accept through the API!
        boolean isProperType = uploadedFileData.isProperType() && (forcedFileType==null || fileType == forcedFileType);

        if (!isProperType || !uploadedFileData.isValid()) {
            String message;
            if (!isProperType) {
                message = wordlet("fileUpload.typeNotAllowed", fileType.getNameForDisplay());
            } else if (!isEmpty(uploadedFileData.getErrorMessage())) {
                message = uploadedFileData.getErrorMessage();
            } else {
                message = wordlet("fileUpload.invalid");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("File upload rejected. isProperType/" + isProperType + " isValid/" + uploadedFileData.isValid() + " errorCode/" + message);
            }

            throw new ApplicationError(message);
        }

        if (uploadedFileData.isTooBig()) {
            int maxSize = uploadedFileData.getFileUsageType().getMaxFileSize(fileType);
            String size = NumberFormat.getIntegerInstance().format((double) maxSize / (IPUtil.BYTES_PER_MB)) + "mb";
            if (logger.isDebugEnabled()) {
                logger.debug("File upload rejected. File too big. size/" + size + " maxSize/" + maxSize);
            }
            throw new ApplicationError(wordlet("fileUpload.tooBig", size, uploadedFileData.getFilename()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("File upload accepted.");
        }
    }
}