package org.narrative.network.shared.authentication;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ClientAgentInformation;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.RequestType;
import org.narrative.network.shared.interceptors.UserSessionRequestType;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionContext;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 5, 2005
 * Time: 5:12:29 PM
 */
public abstract class UserSession<SessionKey> {

    private static final NetworkLogger logger = new NetworkLogger(UserSession.class);

    /**
     * bl: the threadLocalSession is used for both RSS requests and also for shared sessions for search
     * engine spiders.  note that the two cases are mutually exclusive.
     */
    private static final ThreadLocal<UserSession> threadLocalSession = new ThreadLocal<>();

    private final long creationTime;

    protected final OID uniqueVisitOid;
    protected final SessionKey sessionKey;
    private Locale locale;
    private boolean areCookiesDisabledByRecognizedNonSearchSpiderClient;
    private Boolean clusterAdmin;
    private boolean isTemporarySessionDueToFirstRequestBeingPost;
    private boolean isTemporarySessionForSignedRequest;
    private boolean isSessionExpired;
    private final Map<Object, Object> sessionObjects = new HashMap<>();
    private boolean isSharedSearchEngineSpiderSession = false;

    private final ClientAgentInformation clientAgentInformation;
    private static final String USER_SESSION_KEY_PREFIX = "userSession_";

    /**
     * Constructor for use by subclasses for Jackson de-serialization
     */
    protected UserSession(long creationTime, @NotNull Locale locale, @NotNull OID uniqueVisitOid, ClientAgentInformation clientAgentInformation, SessionKey sessionKey) {
        this.creationTime = creationTime;
        this.uniqueVisitOid = uniqueVisitOid;
        this.sessionKey = sessionKey;
        this.locale = locale;
        this.clientAgentInformation = clientAgentInformation;
    }

    protected UserSession(@NotNull FormatPreferences formatPreferences, @NotNull OID uniqueVisitOid, @NotNull RequestResponseHandler reqResp, SessionKey sessionKey) {
        this(System.currentTimeMillis(), formatPreferences.getLocale(), uniqueVisitOid, reqResp.getClientAgentInformation(), sessionKey);
    }

    /**
     * get the unique visit OID for this session.
     * this visit OID is used to track a user's visit (browser session)
     * across all of the sites on the network.
     *
     * @return the unique visit OID for this UserSession.
     */
    @NotNull
    public OID getUniqueVisitOid() {
        return uniqueVisitOid;
    }

    public static boolean hasSession() {
        return getUserSession() != null;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public ClientAgentInformation getClientAgentInformation() {
        return clientAgentInformation;
    }

    public boolean isAreCookiesDisabledByRecognizedNonSearchSpiderClient() {
        return areCookiesDisabledByRecognizedNonSearchSpiderClient;
    }

    public void setAreCookiesDisabledByRecognizedNonSearchSpiderClient(boolean areCookiesDisabledByRecognizedNonSearchSpiderClient) {
        this.areCookiesDisabledByRecognizedNonSearchSpiderClient = areCookiesDisabledByRecognizedNonSearchSpiderClient;
    }

    public boolean isLoggedInClusterAdmin() {
        return clusterAdmin != null && clusterAdmin;
    }

    public void setClusterAdmin(Boolean clusterAdmin) {
        this.clusterAdmin = clusterAdmin;
    }

    public boolean isTemporarySessionDueToFirstRequestBeingPost() {
        return isTemporarySessionDueToFirstRequestBeingPost;
    }

    public void setTemporarySessionDueToFirstRequestBeingPost(boolean temporarySessionDueToFirstRequestBeingPost) {
        isTemporarySessionDueToFirstRequestBeingPost = temporarySessionDueToFirstRequestBeingPost;
    }

    public boolean isTemporarySessionForSignedRequest() {
        return isTemporarySessionForSignedRequest;
    }

    public void setTemporarySessionForSignedRequest(boolean temporarySessionForSignedRequest) {
        isTemporarySessionForSignedRequest = temporarySessionForSignedRequest;
    }

    public boolean isSessionExpired() {
        return isSessionExpired;
    }

    public void setSessionExpired(boolean sessionExpired) {
        isSessionExpired = sessionExpired;
    }

    public boolean isOlderThan30Minutes() {
        return (creationTime + 30 * IPDateUtil.MINUTE_IN_MS) < System.currentTimeMillis();
    }

    public <T> T removeSessionObject(Object key) {
        return (T) sessionObjects.remove(key);
    }

    public void setSessionObject(Object key, Object value) {
        if (value == null) {
            removeSessionObject(key);
        } else {
            sessionObjects.put(key, value);
        }
    }

    public <T> T getSessionObject(Object key) {
        return (T) sessionObjects.get(key);
    }

    protected boolean isSharedSearchEngineSpiderSession() {
        return isSharedSearchEngineSpiderSession;
    }

    public void setSharedSearchEngineSpiderSession(boolean sharedSearchEngineSpiderSession) {
        this.isSharedSearchEngineSpiderSession = sharedSearchEngineSpiderSession;
    }

    /**
     * bl: for spiders, we want to share the same UserSession objects, so just set the UserSession
     * via a ThreadLocal and getUserSession will handle looking it up.
     */
    public static void setCurrentSessionAsThreadLocal(UserSession userSession) {
        threadLocalSession.set(userSession);
    }

    public static void setCurrentSessionOnHttpSession(UserSession userSession) {
        // add it to the session
        assert !hasSession() : "A session has already been set for this user in this area.  Coding error.";
        updateCurrentSessionOnHttpSession(userSession);
    }

    public static void updateCurrentSessionOnHttpSession(UserSession userSession) {
        actionContext().getSession().put(userSession.getSessionKey(), userSession);
    }

    public static void terminateCurrentSessionAndStartNewSession(UserSession userSessionToStart) {
        // bl: first, terminate any session with the same session key
        terminateSession(userSessionToStart.getSessionKey());
        setCurrentSessionOnHttpSession(userSessionToStart);
    }

    public static void terminateCurrentSessionAndStartNewSession(UserSession userSessionToStart, HttpSession httpSession) {
        // bl: just set the new UserSession on the HttpSession, and we're done.
        httpSession.setAttribute(userSessionToStart.getSessionKey(), userSessionToStart);
    }

    public void terminateSession() {
        terminateSession(getSessionKey());
    }

    private static void terminateSession(String sessionKey) {
        actionContext().getSession().remove(sessionKey);
        if (hasSession()) {
            UserSession threadLocalSession = getUserSession();
            if (isEqual(threadLocalSession.getSessionKey(), sessionKey)) {
                terminateThreadLocalSession();
            }
        }
    }

    /**
     * terminate a thread local session.  optionally can unbind the session immediately,
     * thus calling valueUnbound on the UserSession object.  useful for "one-off" UserSession
     * objects created for a single request.
     */
    public static void terminateThreadLocalSession() {
        terminateThreadLocalSession(false);
    }

    public static void terminateThreadLocalSession(boolean expectNoSession) {
        UserSession userSession = threadLocalSession.get();
        // bl: don't unbind the shared search engine spider sessions at the end of the request.
        if (userSession != null) {
            if (expectNoSession) {
                Throwable t = new Throwable("Found a left-over UserSession from a previous request! clearing it, but you might want to look at this.");
                StatisticManager.recordException(t, false, isNetworkContextSet() ? networkContext().getReqResp() : null);
                logger.error("Session found", t);
            }
            // bl: even if valueUnbound fails, make sure that we at least remove the thread local session
            // to prevent screwing up future threads.
            threadLocalSession.remove();
        }

    }

    public abstract OID getRoleOid();

    public abstract boolean isLoggedInUser();

    protected String getSessionKeySuffix() {
        // bl: the default implementation comes from UserSessionRequestType
        return UserSessionRequestType.getRequestTypeFromSession(this).getSessionKeySuffix();
    }

    private String getSessionKey() {
        return USER_SESSION_KEY_PREFIX + getSessionKeySuffix();
    }

    private static String getCurrentSessionKey() {
        /* bl: this wasn't working for master-personal requests.
        Area area = currentArea();
        String suffix;
        if (exists(area)) {
            suffix = area.getOid().toString();
        } else {
            suffix = "master";
        }*/

        NetworkContext networkContext = networkContext();
        RequestType requestType = networkContext.getRequestType();

        return getSessionKey(requestType.getUserSessionRequestType().getSessionKeySuffix());
    }

    public static String getSessionKey(String suffix) {
        return USER_SESSION_KEY_PREFIX + suffix;
    }

    protected static UserSession getThreadLocalUserSession() {
        return threadLocalSession.get();
    }

    public static UserSession getUserSession(UserSessionRequestType requestType) {
        UserSession userSession = getUserSession();
        if (userSession == null) {
            return null;
        }

        if (requestType != null) {
            assert requestType.getUserSessionClass().isAssignableFrom(userSession.getClass()) : "Can't attempt to get a UserSession for a UserSessionRequestType unless it is guaranteed to be of the proper type!  Was a thread local session not cleaned up correctly? us/" + userSession + " cls/" + userSession.getClass().getName() + " expected/" + requestType.getUserSessionClass() + " tlus/" + getThreadLocalUserSession();
        }

        return userSession;
    }

    public static UserSession<?> getUserSession() {
        // bl: changed to check the thread local first.  this way, if there is a ThreadLocal
        // UserSession, we'll use it first (always).  without doing the checks in this order,
        // whenever a session was invalidated with RequestResponseHandler.terminateSession(),
        // any call to the SessionMap.get() method would result in an IllegalStateException
        // because we'd be trying to call getAttribute() on a session that had already
        // been invalidated.
        // the only legitimate scenario for this case is when a user makes a POST request
        // and as a result of the POST request, we detect that the session is no longer
        // valid (due to an IP change, a user-agent change, etc.).  in that case, we terminate
        // the current session and create a new UserSession and set it as a ThreadLocal
        // for the current request only.  the UserSession has the isTemporarySessionDueToFirstRequestBeingPost
        // flag set to true in this case so that we can give the user the appropriate error page.
        UserSession us = getThreadLocalUserSession();
        if (us != null) {
            return us;
        }

        if (!isNetworkContextSet()) {
            return null;
        }

        return getUserSessionFromHttpSession();
    }

    public static UserSession<?> getUserSessionFromHttpSession() {
        ActionContext actionContext = actionContext();
        if (actionContext != null) {
            Map actionSession = actionContext.getSession();
            if (actionSession != null) {
                // bl: support detection of an already invalidated HttpSession.  if it happens, then just ignore
                // the exception and return null.
                try {
                    return (UserSession) actionSession.get(getCurrentSessionKey());
                } catch (IllegalStateException ise) {
                    // ignore
                }
            }
        }
        return null;
    }

    private static final String CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY = UserSession.class.getName() + "-ConfirmationMessageUrlMap";

    public void registerConfirmationMessage(String targetUrl, OID originalProcessOid) {
        // bl: first register the URL/OID in the session map
        Map<String, OID> urlToProcessOid = getSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY);
        if (urlToProcessOid == null) {
            synchronized (this) {
                urlToProcessOid = getSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY);
                if (urlToProcessOid == null) {
                    setSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY, urlToProcessOid = newConcurrentHashMap());
                }
            }
        }
        urlToProcessOid.put(targetUrl, originalProcessOid);
    }

    public OID getConfirmationMessageProcessOidForUrl(String url) {
        if (isEmpty(url)) {
            return null;
        }
        Map<String, OID> urlToProcessOid = getSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY);
        if (urlToProcessOid == null) {
            return null;
        }
        OID originalProcessOid = urlToProcessOid.remove(url);
        // bl: special case to handle a trailing slash on the URL (which browsers may include, but we don't register that way
        if (originalProcessOid == null) {
            url = IPStringUtil.getStringAfterStripFromEnd(url, "/");
            originalProcessOid = urlToProcessOid.remove(url);
            // bl: for legacy UIs, check for the rl=true param on the end of the URL
            if (originalProcessOid == null) {
                // bl: if the URL ends with rl=true, then we should try stripping it off the end
                if (url.endsWith("rl=true")) {
                    url = IPStringUtil.getStringAfterStripFromEnd(url, "rl=true");
                    url = IPStringUtil.getStringAfterStripFromEnd(url, "&");
                    url = IPStringUtil.getStringAfterStripFromEnd(url, "?");
                    return getConfirmationMessageProcessOidForUrl(url);
                } else {
                    // bl: otherwise if the URL doesn't end with rl=true, we should try adding it on to handle legacy UIs
                    // that did a client-side redirect with rl=true appended to the end
                    url = IPHTMLUtil.getParametersAsURL(url, Collections.singletonMap("rl", "true"));
                    originalProcessOid = urlToProcessOid.remove(url);
                }
            }
        }
        return originalProcessOid;
    }

    public void copyConfirmationMessageMap(UserSession<?> originalUserSession) {
        Map<String, OID> urlToProcessOid = originalUserSession.getSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY);
        if (urlToProcessOid != null) {
            setSessionObject(CONFIRMATION_MESSAGE_URL_MAP_SESSION_KEY, urlToProcessOid);
        }
    }

    private static final String VERIFIED_EMAILS_SESSION_KEY = UserSession.class.getName() + "-VerifiedEmails";

    public void verifyEmailAddress(String emailAddress) {
        if (isEmpty(emailAddress)) {
            return;
        }

        Set<String> verifiedEmails = getSessionObject(VERIFIED_EMAILS_SESSION_KEY);
        if (verifiedEmails == null) {
            setSessionObject(VERIFIED_EMAILS_SESSION_KEY, verifiedEmails = new HashSet<>());
        }

        verifiedEmails.add(emailAddress.toLowerCase());
    }

    public boolean isEmailVerifiedWithinSession(String emailAddress) {
        if (isEmpty(emailAddress)) {
            return false;
        }

        Set<String> verifiedEmails = getSessionObject(VERIFIED_EMAILS_SESSION_KEY);
        if (verifiedEmails == null) {
            return false;
        }

        return verifiedEmails.contains(emailAddress.toLowerCase());
    }
}
