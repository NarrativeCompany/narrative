package org.narrative.network.shared.struts;

import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.struts2.dispatcher.multipart.JakartaMultiPartRequest;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 6/25/15
 * Time: 2:05 PM
 *
 * @author brian
 */
public class NetworkMultiPartRequest extends JakartaMultiPartRequest {
    private static final NetworkLogger logger = new NetworkLogger(NetworkMultiPartRequest.class);

    @Override
    public void parse(HttpServletRequest request, String saveDir) throws IOException {
        try {
            setLocale(request);
            processUpload(request, saveDir);
        } catch (FileUploadBase.SizeLimitExceededException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Request exceeded size limit!", e);
            }
            String errorMessage = buildErrorMessage(e, new Object[]{e.getPermittedSize(), e.getActualSize()});
            if (!errors.contains(errorMessage)) {
                errors.add(errorMessage);
            }
        } catch (Exception e) {
            StatisticManager.recordException(e, false, isNetworkContextSet() ? networkContext().getReqResp() : null);
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to parse request", e);
            }
            String errorMessage = buildErrorMessage(e, new Object[]{});
            if (!errors.contains(errorMessage)) {
                errors.add(errorMessage);
            }
        }
    }
}
