package org.narrative.network.core.cluster.actions.server;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.narrative.common.util.HtmlConstants;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.cluster.actions.ServerStatusTask;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.baseactions.NetworkAction;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Aug 30, 2006
 * Time: 2:17:23 PM
 */
public class ServerStatusAction extends NetworkAction {

    private ServerStatusTask serverStatus;

    private String servletName = null;
    private boolean isProxy = false;

    private String contentType;
    private String stringInput;

    public ServerStatusAction() {}

    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String input() throws Exception {
        // if this request is for this server, then return the result
        // bl: assume request was intended for this server if no hostname specified.  pound can load balance
        // to the proper server for our XML requests based on FQDN.
        if (isEmpty(servletName) || NetworkRegistry.getInstance().getServletName().equals(servletName)) {
            serverStatus = new ServerStatusTask();
            getNetworkContext().doGlobalTask(serverStatus);
            return INPUT;

            // if this is already a proxy request, and it wasn't the right server, then just bail to avoid the infinite redirect.
            // Something's wrong with the dns or something
        } else if (isProxy) {
            throw UnexpectedError.getRuntimeException("Error!  Attempted a proxy call to a different server, but the server name and hostname must have mismatched!  Some kind of DNS or server naming issue. serverName param: " + servletName + " actual serverName: " + NetworkRegistry.getInstance().getServletName() + " host: " + getNetworkContext().getReqResp().getHost());
            // otherwise proxy this result to another server
        } else {
            HttpState state = new HttpState();
            for (javax.servlet.http.Cookie cookie : getNetworkContext().getReqResp().getCookies()) {
                state.addCookie(new Cookie(cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getMaxAge(), cookie.getSecure()));
            }
            HttpClient client = IPHTMLUtil.getDefaultHttpClient();
            client.setState(state);
            GetMethod getMethod = new GetMethod(NetworkRegistry.getInstance().getDirectServerBaseUrlForServletName(servletName, true) + requestURI + "/proxy/true");
            // bl: simulate as an AJAX request
            getMethod.setRequestHeader("X-Requested-With", "XMLHttpRequest");
            try {
                client.executeMethod(getMethod);

                // bl: changed to just write the string locally into a StringBuffer.  we'll then write that string
                // to the output via the new NetworkStringResult.
                if (getMethod.getStatusCode() >= 400) {
                    stringInput = "Warning: failed fetching remote server status. \"" + getMethod.getStatusText() + "\" (" + getMethod.getStatusCode() + ")";
                    contentType = HtmlConstants.MIME_PLAIN;
                } else {
                    stringInput = getMethod.getResponseBodyAsString();
                    Header header = getMethod.getResponseHeader("Content-Type");
                    // bl: default to "text/html" as the contentType if none found
                    contentType = header != null ? header.getValue() : HtmlConstants.MIME_HTML;
                }
            } finally {
                getMethod.releaseConnection();
            }
            return SUCCESS;
        }

    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public void setProxy(boolean proxy) {
        isProxy = proxy;
    }

    public ServerStatusTask getServerStatus() {
        return serverStatus;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStringInput() {
        return stringInput;
    }
}
