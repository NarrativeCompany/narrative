package org.narrative.network.shared.struts;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.shared.services.DelayedResultRunnable;
import org.narrative.network.shared.services.NetworkServletDispatcherResult;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Oct 25, 2007
 * Time: 9:27:18 AM
 *
 * @author brian
 */
public class NetworkStringResult extends StrutsResultSupport {
    private static final NetworkLogger logger = new NetworkLogger(DelayedResultRunnable.class);

    private static final int BUFFER_SIZE = 1024;
    private static final String STRING_INPUT_OGNL_STACK_NAME = "stringInput";

    private static final String SPECIAL_EMPTY_RESPONSE_IDENTIFIER = "NetworkStringResultEmptyResponse";

    private String contentType;
    private String contentDisposition;
    private String stringResultValue;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public void setStringResultValue(String stringResultValue) {
        this.stringResultValue = stringResultValue;
    }

    public void doExecute(String s, ActionInvocation actionInvocation) throws Exception {
        // bl: allowing this value to be set two different ways.  first, it can be set via the "stringResultValue" param
        // in the result definition in the Struts config XML.  if a value is not set there, then we will look for
        // a "stringInput" value on the OGNL stack.
        String value = conditionalParse(stringResultValue, actionInvocation);
        // bl: handle the special empty response identifier.
        if (SPECIAL_EMPTY_RESPONSE_IDENTIFIER.equalsIgnoreCase(value)) {
            value = "";
        }
        if (value == null) {
            value = (String) actionInvocation.getStack().findValue(conditionalParse(STRING_INPUT_OGNL_STACK_NAME, actionInvocation));
            if (value == null) {
                value = "";
            }
        }

        String fValue = value;

        String contentDisposition = this.contentDisposition != null ? conditionalParse(this.contentDisposition, actionInvocation) : null;
        String contentType = this.contentType != null ? conditionalParse(this.contentType, actionInvocation) : null;

        RequestResponseHandler reqResonseHandler = networkContext().getReqResp();

        HttpServletResponse response = (HttpServletResponse) actionInvocation.getInvocationContext().get(HTTP_RESPONSE);
        DelayedResultRunnable.process(() -> {
            InputStream is = null;
            OutputStream os = null;
            try {
                try {
                    byte[] bytes = fValue.getBytes(IPUtil.IANA_UTF8_ENCODING_NAME);
                    is = new ByteArrayInputStream(bytes);

                    // Set the content-disposition
                    if (contentDisposition != null) {
                        response.addHeader("Content-disposition", contentDisposition);
                    }

                    if (contentType != null) {
                        response.setContentType(contentType);
                    }
                    response.setContentLength(bytes.length);
                    NetworkServletDispatcherResult.setNoCacheHeadersIfNecessary(reqResonseHandler, actionInvocation);

                    os = response.getOutputStream();

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (Throwable t) {
                throw UnexpectedError.getRuntimeException("Failed sending String response: \"" + IPStringUtil.getTruncatedString(fValue, 255) + "\" for action/" + IPUtil.getClassSimpleName(actionInvocation.getAction().getClass()), t);
            }
        }, () -> {
            logger.error("Failed processing NetworkStringResult! value/" + IPStringUtil.getTruncatedString(fValue, 255), new Throwable());
        });
    }
}
