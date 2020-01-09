package org.narrative.network.shared.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.shared.authentication.UserSession;

import java.util.Arrays;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/24/14
 * Time: 2:08 PM
 */
public class HttpPostSessionObject {
    private final Map<Object, HttpPostFormData> formDataLookup = newConcurrentHashMap();

    private HttpPostSessionObject() {}

    /**
     * Returns null if there was no form data in the request, or that form data expired
     */
    public HttpPostFormData getFormData(String formType, RequestResponseHandler requestHandler, UserSession session, boolean createIfNotFound) {
        return getFormData(formType, requestHandler.getParams(), session, createIfNotFound);
    }

    public HttpPostFormData getFormData(String formType, Map paramMap, UserSession session, boolean createIfNotFound) {
        // jw: we will use the formType and a hash of all of the form parameters sans the form OID and MD5, so that if a member submits the same effective form multiple times we will
        //     still catch it. We had a customer submit a create ClipSet form, then load a new tab and submit effectively the same form again, so this new method will
        //     catch that scenario and better ensure that we are detecting two effectively equal forms.
        // bl: in order to make jackson serialization easier, just going to use an array of the objects here instead
        // of using a type like ObjectPair or Pair (commons lang) that would otherwise require a custom deserializer
        Object key = new Object[]{formType, getParameterMapHashCode(paramMap)};

        HttpPostFormData formData = formDataLookup.get(key);
        if (formData != null && !formData.isExpired()) {
            return formData;
        }

        synchronized (session) {
            formData = formDataLookup.get(key);
            if (formData != null && !formData.isExpired()) {
                return formData;
            }

            if (!createIfNotFound) {
                return null;
            }

            // Guess we need to create and register the form data
            final HttpPostFormData fFormData = new HttpPostFormData();
            formDataLookup.put(key, fFormData);

            // if the request fails lets mark the form data as such
            PartitionGroup.addEndOfPartitionGroupRunnableForError(() -> {
                synchronized (session) {
                    formDataLookup.remove(key);
                }
            });
            // if the request succeeds lets start the timer on the form data
            PartitionGroup.addEndOfPartitionGroupRunnable(fFormData::requestCompleted);

            return null;
        }
    }

    private static final String LAST_HTTP_POST_DATA_SESSION_KEY = "lastHttpPostDataSessionKey";

    public static HttpPostSessionObject getForUserSession(UserSession<?> userSession) {
        assert userSession != null : "Should never get this far into the code without having a userSession.";

        HttpPostSessionObject sessionObject = userSession.getSessionObject(LAST_HTTP_POST_DATA_SESSION_KEY);
        if (sessionObject != null) {
            return sessionObject;
        }
        synchronized (userSession) {
            sessionObject = userSession.getSessionObject(LAST_HTTP_POST_DATA_SESSION_KEY);
            if (sessionObject == null) {
                userSession.setSessionObject(LAST_HTTP_POST_DATA_SESSION_KEY, sessionObject = new HttpPostSessionObject());
            }

            return sessionObject;
        }
    }

    /**
     * JW: there is a inherent issue with getting a hashcode from a parameter map (String => String[]) because .hashcode()
     * on arrays is not accurate.  The Map.hashcode() is doing exactly that on the array objects stored in the values
     * of the map.  To solve this I am going to create a new map first where its parameter name to Arrays.hashCode
     * so that we will be sure to get the same hashcode for value arrays with the same values.
     */
    private static int getParameterMapHashCode(Map<String, String[]> params) {
        Map<String, Integer> hashableMap = newTreeMap();
        for (Map.Entry<String, String[]> paramEntry : params.entrySet()) {
            hashableMap.put(paramEntry.getKey(), Arrays.hashCode(paramEntry.getValue()));
        }

        return hashableMap.hashCode();
    }

    public static class HttpPostFormData {
        private Long expiration;
        private String redirect;

        private void requestCompleted() {
            // jw: lets set the expiration milliseconds to one minute in the future.  This means that we will only consider
            //     this HttpPostFormData object valid for one minute after success.
            expiration = System.currentTimeMillis() + IPDateUtil.MINUTE_IN_MS;
        }

        private boolean isExpired() {
            // this form data is valid as long as the initiating request is processing or its within a minute of it finishing
            return expiration != null && expiration < System.currentTimeMillis();
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }
    }
}
