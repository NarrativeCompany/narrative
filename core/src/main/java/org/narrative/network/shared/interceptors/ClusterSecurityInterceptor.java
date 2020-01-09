package org.narrative.network.shared.interceptors;

import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.IPUtil;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.cluster.actions.server.ServerStatusAction;
import org.narrative.network.shared.authentication.ClusterUserSession;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.security.ClusterRole;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Feb 23, 2015
 * Time: 3:07:00 PM
 *
 * @author Brian
 */
public class ClusterSecurityInterceptor extends NetworkStrutsInterceptorBase {

    private static final NetworkLogger logger = new NetworkLogger(ClusterSecurityInterceptor.class);

    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        Object action = actionInvocation.getAction();
        // bl: no security required for ServerStatusAction
        if (action instanceof ServerStatusAction) {
            return actionInvocation.invoke();
        }
        RequestResponseHandler reqResp = getNetworkContext().getReqResp();
        ClusterUserSession userSession = ClusterUserSession.getClusterUserSession();
        // bl: if this is a new session, then we need to authenticate!
        if (userSession == null) {
            String username = null;
            if(getNetworkContext().getRequestType().isClusterCp()) {
                // todo: figure out cluster CP security
                username = "admin";
            } else {
                assert getNetworkContext().getRequestType().isKycQueue() : "Found unexpected RequestType/" + getNetworkContext().getRequestType() + " in ClusterSecurityInterceptor!";
                ObjectPair<String,String> basicAuthUsernameAndPassword = getBasicAuthenticationUsernamePassword();
                if(basicAuthUsernameAndPassword!=null) {
                    // bl: check the username and password against the users in properties
                    String submittedUsername = basicAuthUsernameAndPassword.getOne();
                    String password = basicAuthUsernameAndPassword.getTwo();
                    NarrativeProperties properties = StaticConfig.getBean(NarrativeProperties.class);
                    Map<String,String> users = properties.getKycQueue().getUsers();
                    if(users!=null) {
                        String expectedPassword = users.get(submittedUsername);
                        // auth is successful if the password matches!
                        if(isEqual(password, expectedPassword)) {
                            username = submittedUsername;
                        }
                    }
                }

                // if we didn't find a username, then auth was not successful, so prompt for HTTP basic auth
                if(isEmpty(username)) {
                    return basicAuthRequiredResponse(reqResp, "KYC Queue Admin");
                }
            }

            // bl: at this point, the request is valid, so make sure we have a role and session!
            ClusterRole clusterRole = new ClusterRole(username);
            userSession = new ClusterUserSession(clusterRole, OIDGenerator.getNextOID(), reqResp);
            UserSession.setCurrentSessionOnHttpSession(userSession);
        }
        getNetworkAction().setUserSession(userSession);
        // bl: set up the cluster role so that cluster admins can upload files (which require a PrimaryRole in FileUploadUtils).
        getNetworkContext().setPrimaryRole(userSession.getClusterRole());
        // bl: do the check right!
        getNetworkAction().checkRight();
        return actionInvocation.invoke();
    }

    private static String basicAuthRequiredResponse(RequestResponseHandler reqResp, String realm) {
        // no authorization header? then we need to prompt for authorization
        reqResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        reqResp.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        return NetworkResponses.emptyResponse();
    }

    private static String unauthorizedResponse(RequestResponseHandler reqResp) {
        reqResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return NetworkResponses.emptyResponse();
    }

    private static ObjectPair<String,String> getBasicAuthenticationUsernamePassword() {
        RequestResponseHandler reqResp = networkContext().getReqResp();
        String authHeader = reqResp.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), IPUtil.IANA_UTF8_ENCODING_NAME);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String username = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();

                            return new ObjectPair<>(username, password);
                        } else {
                            logger.error("Invalid authentication token: \"" + credentials + "\"");
                        }
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Encoding issue extracting authentication token", e);
                    }
                }
            }
        }
        return null;
    }

}
