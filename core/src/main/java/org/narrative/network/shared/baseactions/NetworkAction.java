package org.narrative.network.shared.baseactions;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.BaseValidationHandler;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.StrutsUtil;
import org.narrative.common.util.ValidationError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.common.util.processes.ActionProcess;
import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.authentication.UserSessionAware;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextAware;
import org.narrative.network.shared.services.ConfirmationMessage;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 17, 2005
 * Time: 12:46:59 AM
 * <p>
 * bl: making this no longer abstract so that this action can be used for anything that wants
 * to have some default behavior.  for now, just using for a favicon.ico action.
 */
public class NetworkAction extends ActionSupport implements NetworkContextAware, UserSessionAware, RequestResponseAware, ValidationHandler {

    private NetworkContext networkContext;
    private UserSession userSession;

    /**
     * an LRUMap in which to keep all of the most recent confirmation messages.
     * we track a confirmation message on a per process basis (optionally).  a process/action
     * can set the message by calling registerConfirmationMessage.  the message will then be
     * added to the map.  on a subsequent request (redirect more than likely) the action
     * will pull the originalProcessOid out of the UserSession, which can then be used
     * to pull the ConfirmationMessage out of this map.
     * <p>
     * this map should take care of cleaning itself up.
     * <p>
     * todo: do we need to clean entries out of this map after a certain amount of time?
     * todo: sizing of this map?  leaving at the default max size of 1000 for now.
     * todo: this won't work across servlets, so if a confirmation message needs to be supplied
     * across domains (and thus potentially servlets), this isn't the right solution.
     * i think that is probably the more rare case, so designing like this for now.
     */
    private static final org.narrative.common.util.LRUMap<OID, ConfirmationMessage> PROCESS_IDS_TO_CONFIRMATION_MESSAGES = new org.narrative.common.util.LRUMap<OID, ConfirmationMessage>();

    protected static final String CONFIRMATION_MESSAGE_PROCESS_OID_PARAM = "confirmationMessageProcessOid";

    private ConfirmationMessage confirmationMessageForSubsequentRequest;
    private ConfirmationMessage confirmationMessage;

    private OID confirmationMessageProcessOid;

    public static final String FORM_TYPE_PARAM_NAME = "formType";

    private String formType;
    protected String requestURI;
    protected String queryString;
    protected String requestUrl;

    private String ajaxDivPopupId;

    public static final String REDIRECT_PARAM = "redirect";

    protected String redirect;
    private final NetworkActionValidationHandler validationHandler = new NetworkActionValidationHandler(this);

    public NetworkRegistry getNetworkRegistry() {
        return NetworkRegistry.getInstance();
    }

    public void setNetworkContext(NetworkContext networkContext) {
        this.networkContext = networkContext;
    }

    public NetworkContext getNetworkContext() {
        return networkContext;
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    public UserSession<?> getUserSession() {
        return userSession;
    }

    public void setRequestResponse(RequestResponseHandler reqResp) {
        assert networkContext.getReqResp() == reqResp : "Attempting to set a different HttpServletRequestResponseHandler on NetworkAction than was set previously on the NetworkContext!  Coding error!";
        requestURI = reqResp.getURI();
        queryString = reqResp.getQueryString();
        // bl: get the url up front so that we can re-use it in the JSP.  can't use RequestResponseHandler.getUrl()
        // in the JSP because it will give us the wrong URL (the URL to the JSP file instead of the original url).
        requestUrl = reqResp.getUrl();
    }

    public void checkRight() {
    }

    public void checkRightAfterParams() {
    }

    @Override
    public final boolean validateNumber(Number value, Number min, Number max, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNumber(value, min, max, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateNumberWithLabel(Number value, Number min, Number max, String fieldName, String fieldWordletName) {
        return validationHandler.validateNumberWithLabel(value, min, max, fieldName, fieldWordletName);
    }

    @Override
    public final boolean validateNumberMin(Number value, Number min, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNumberMin(value, min, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateNumberMinWithLabel(Number value, Number min, String fieldName, String fieldWordletName) {
        return validationHandler.validateNumberMinWithLabel(value, min, fieldName, fieldWordletName);
    }

    @Override
    public final boolean validateNumberMax(Number value, Number max, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNumberMax(value, max, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateNumberMaxWithLabel(Number value, Number max, String fieldName, String fieldWordletName) {
        return validationHandler.validateNumberMaxWithLabel(value, max, fieldName, fieldWordletName);
    }

    @Override
    public final boolean validateNotNull(Object value, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNotNull(value, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateNotNullWithLabel(Object value, String fieldName, String labelName) {
        return validationHandler.validateNotNullWithLabel(value, fieldName, labelName);
    }

    @Override
    public boolean validateBigDecimal(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateBigDecimal(value, min, max, fieldName, fieldWordletName, args);
    }

    @Override
    public boolean validateBigDecimalWithLabel(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName, String labelName) {
        return validationHandler.validateBigDecimalWithLabel(value, min, max, fieldName, labelName);
    }

    @Override
    public final boolean validateExists(Object value, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateExists(value, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateExistsWithLabel(Object value, String fieldName, String labelName) {
        return validationHandler.validateExistsWithLabel(value, fieldName, labelName);
    }

    @Override
    public final boolean validateNotEmpty(String value, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNotEmpty(value, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateNotEmptyWithLabel(String value, String fieldName, String labelName) {
        return validationHandler.validateNotEmptyWithLabel(value, fieldName, labelName);
    }

    @Override
    public boolean validateNotEmptyOrNull(Collection value, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateNotEmptyOrNull(value, fieldName, fieldWordletName, args);
    }

    @Override
    public boolean validateNotEmptyOrNullWithLabel(Collection value, String fieldName, String labelName) {
        return validationHandler.validateNotEmptyOrNullWithLabel(value, fieldName, labelName);
    }

    @Override
    public final boolean validateString(String value, int min, int max, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateString(value, min, max, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateStringWithLabel(String value, int min, int max, String fieldName, String labelName) {
        return validationHandler.validateStringWithLabel(value, min, max, fieldName, labelName);
    }

    @Override
    public final boolean validateString(String value, String pattern, int min, int max, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateString(value, pattern, min, max, fieldName, fieldWordletName, args);
    }

    @Override
    public final boolean validateStringWithLabel(String value, String pattern, int min, int max, String fieldName, String labelName) {
        return validationHandler.validateStringWithLabel(value, pattern, min, max, fieldName, labelName);
    }

    @Override
    public boolean validateString(String value, Pattern pattern, String fieldName, String fieldWordletName, Object... args) {
        return validationHandler.validateString(value, pattern, fieldName, fieldWordletName, args);
    }

    @Override
    public boolean validateStringWithLabel(String value, Pattern pattern, String fieldName, String labelName) {
        return validationHandler.validateStringWithLabel(value, pattern, fieldName, labelName);
    }

    @Override
    public final void addWordletizedActionError(String anErrorMessage, Object... args) {
        validationHandler.addWordletizedActionError(anErrorMessage, args);
    }

    @Override
    public final void addWordletizedRequiredFieldError(String fieldName, String fieldWordletName, Object... args) {
        validationHandler.addWordletizedRequiredFieldError(fieldName, fieldWordletName, args);
    }

    @Override
    public void addRequiredFieldError(String fieldName, String fieldLabel) {
        validationHandler.addRequiredFieldError(fieldName, fieldLabel);
    }

    @Override
    public final void addWordletizedInvalidFieldError(String fieldName, String fieldWordletName, Object... args) {
        validationHandler.addWordletizedInvalidFieldError(fieldName, fieldWordletName, args);
    }

    @Override
    public void addInvalidFieldError(String fieldName, String fieldLabel) {
        validationHandler.addInvalidFieldError(fieldName, fieldLabel);
    }

    @Override
    public final void addWordletizedFieldError(String fieldName, String errorMessage, Object... args) {
        validationHandler.addWordletizedFieldError(fieldName, errorMessage, args);
    }

    @Override
    public List<ValidationError> getValidationErrors() {
        return validationHandler.getValidationErrors();
    }

    @Override
    public boolean isThrowApplicationErrorOnValidationError() {
        return validationHandler.isThrowApplicationErrorOnValidationError();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * override getText so that we don't use Struts's nasty localization lookup strategy
     *
     * @param aTextName the key to look up
     * @return the localized text
     */
    @Override
    public final String getText(String aTextName) {
        return getNetworkContext().getResourceBundle().getString(aTextName);
    }

    @Override
    public final String getText(String aTextName, List args) {
        return getNetworkContext().getResourceBundle().getString(aTextName, args.toArray());
    }

    protected OID getConfirmationMessageProcessOid() {
        return confirmationMessageProcessOid;
    }

    public void setConfirmationMessageProcessOid(OID confirmationMessageProcessOid) {
        this.confirmationMessageProcessOid = confirmationMessageProcessOid;
    }

    public ConfirmationMessage getConfirmationMessage() {
        if (confirmationMessage == null) {
            if (confirmationMessageProcessOid != null) {
                confirmationMessage = getConfirmationMessageForOriginalProcessOid(confirmationMessageProcessOid);
            }
            // bl: if we couldn't look it up by confirmationMessageProcessOid, then check by request URL
            if (confirmationMessage == null) {
                confirmationMessage = getConfirmationMessageForUrl(getUserSession(), getRequestUrl());
            }
        }
        return confirmationMessage;
    }

    protected void setConfirmationMessage(ConfirmationMessage confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    private static ConfirmationMessage getConfirmationMessageForUrl(UserSession userSession, String url) {
        if (userSession != null) {
            // bl: check for a confirmation message for this URL
            OID originalProcessOid = userSession.getConfirmationMessageProcessOidForUrl(url);
            // bl: if we found an originalProcessOid for this URL, then set the ConfirmationMessage from that.
            if (originalProcessOid != null) {
                return getConfirmationMessageForOriginalProcessOid(originalProcessOid);
            }
        }
        return null;
    }

    private static ConfirmationMessage getConfirmationMessageForOriginalProcessOid(OID originalProcessOid) {
        return PROCESS_IDS_TO_CONFIRMATION_MESSAGES.remove(originalProcessOid);
    }

    protected ConfirmationMessage getConfirmationMessageForSubsequentRequest() {
        return confirmationMessageForSubsequentRequest;
    }

    /**
     * set a confirmation message to be used on a subsequent redirect for this action.
     * don't use this if displaying a result directly from this action.
     * use it only if not displaying a result directly from this action.
     * in that case, the confirmationMessage will automatically be associated
     * with a subsequent request by looking up the originalProcessOid in the UserSession map.
     *
     * @param confirmationMessage the confirmation message to use for this action.
     */
    public void setConfirmationMessageForSubsequentRequest(ConfirmationMessage confirmationMessage) {
        confirmationMessageForSubsequentRequest = confirmationMessage;
    }

    /**
     * bl: this method exists simply to unify the places that we are getting the processOid used for ConfirmationMessages.
     *
     * @return the process OID of the current request to use for a ConfirmationMessage on a subsequent request.
     */
    private OID getConfirmationMessageProcessOidForSubsequentRequest() {
        assert confirmationMessageForSubsequentRequest != null : "Should only call this method when a confirmationMessageForSubsequentRequest has already been set!";
        return getProcessOid();
    }

    private boolean isValidConfirmationMessageRedirectUrl(String redirectUrl) {
        // bl: we don't want to register ConfirmationMessages for URLs that are being redirected to other
        // servers. there's no point in registering those redirect URLs.
        // previously, we used a confirmationMessageOidParam, which could break OpenID integration since the
        // confirmationMessageProcessOid included in the URL may get included in the ultimate openid.op_endpoint
        // which will ultimately break the validation.
        try {
            URL url = new URL(redirectUrl);
            // bl: if the current request's domain doesn't match the redirect's domain, then bail out since
            // we won't be able to display the confirmation message anyway.
            if (!IPStringUtil.isStringEqualIgnoreCase(url.getHost(), getNetworkContext().getReqResp().getHost())) {
                return false;
            }
        } catch (MalformedURLException e) {
            // ignore the exception and treat it as invalid
            return false;
        }
        return true;
    }

    public String passConfirmationMessageThroughRedirect(String redirectUrl) {
        ConfirmationMessage confirmationMessage = getConfirmationMessage();
        // bl: if no confirmation message, then leave the redirect URL unchanged
        if (confirmationMessage == null) {
            return redirectUrl;
        }
        setConfirmationMessageForSubsequentRequest(confirmationMessage);
        return registerConfirmationMessage(redirectUrl);
    }

    public String registerConfirmationMessage(String redirectUrl) {
        // bl: check if there is a ConfirmationMessage that we need to persist for a future request
        // bl: nothing to do if there wasn't a ConfirmationMessage already registered.
        if (confirmationMessageForSubsequentRequest == null) {
            return redirectUrl;
        }

        if (!isValidConfirmationMessageRedirectUrl(redirectUrl)) {
            return redirectUrl;
        }

        UserSession userSession = getUserSession();
        assert userSession != null : "Should always have a UserSession in order to register a ConfirmationMessage!";
        if (userSession.isTemporarySessionDueToFirstRequestBeingPost()) {
            // bl: if this is a temporary session due to first request as POST/AJAX, then we need to include
            // it in the URL
            redirectUrl = getRedirectUrlForConfirmationMessage(redirectUrl);
        } else {
            // register the redirectUrl in the user's session
            userSession.registerConfirmationMessage(redirectUrl, getConfirmationMessageProcessOidForSubsequentRequest());
            registerConfirmationMessageByOriginalProcessOid();
        }

        return redirectUrl;
    }

    private void registerConfirmationMessageByOriginalProcessOid() {
        // add the confirmation message to the LRUMap, keyed off of the current process oid.
        PROCESS_IDS_TO_CONFIRMATION_MESSAGES.put(getConfirmationMessageProcessOidForSubsequentRequest(), confirmationMessageForSubsequentRequest);

        // bl: now that we've registered the ConfirmationMessage, let's clear it out so that it's not used again
        confirmationMessageForSubsequentRequest = null;
    }

    public String getRedirectUrlForConfirmationMessage(String redirectUrl) {
        // if it's not a valid redirect URL, then just ignore it.
        if (!isValidConfirmationMessageRedirectUrl(redirectUrl)) {
            // bl: we aren't going to use the ConfirmationMessage, so let's clear it out so it doesn't get used.
            confirmationMessageForSubsequentRequest = null;
            return redirectUrl;
        }
        // bl: get the OID here so we can use it below. the call to register it will clear the confirmationMessageForSubsequentRequest,
        // so it can't be used after that.
        OID confirmationMessageProcessOid = getConfirmationMessageProcessOidForSubsequentRequest();
        registerConfirmationMessageByOriginalProcessOid();
        return IPHTMLUtil.getURLAfterInsertingParameter(redirectUrl, CONFIRMATION_MESSAGE_PROCESS_OID_PARAM, confirmationMessageProcessOid.toString());
    }

    /**
     * get the ActionProcess associated with this action
     *
     * @return the ActionProcess associated with this action
     */
    protected ActionProcess getActionProcess() {
        return ActionProcess.getActionProcess();
    }

    public final String getActionName() {
        String ret = getActionProcess().getInvocation().getProxy().getActionName();
        if (!isEmpty(ret)) {
            return ret;
        }
        // bl: if the action name is empty, then just use the default action name for the namespace
        return StrutsUtil.getDefaultActionNameForNamespace(getActionProcess().getInvocation().getProxy().getNamespace());
    }

    /**
     * get the process oid for this action
     *
     * @return the process oid for this action
     */
    public OID getProcessOid() {
        return getActionProcess().getProcessOid();
    }

    public String getRedirect() {
        // todo: default the redirect to referrer?  current request?
        // it really depends on the application.  hard to say, so leaving as-is for now.
        /*if(redirect==null)
            redirect = getNetworkContext().getReqResp().getReferrer();*/
        return redirect;
    }

    // bl: don't disable html in redirects.  ampersands are valid in redirect URLs, so we don't want them to
    // be converted to &amp;
    @BypassHtmlDisable
    public void setRedirect(String redirect) {
        // bl: special handling to prevent http: from being set as the redirect. this can happen if a URL is requested
        // without the redirect path param properly being escaped.
        if ("http:".equalsIgnoreCase(redirect)) {
            return;
        }
        this.redirect = redirect;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public boolean isTracing() {
        return TraceManager.isTracing();
    }

    public TraceItem getRootTraceItemWithEndAll() {
        TraceManager.endAllTraces();
        return TraceManager.getRootTraceItem();
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public ActionInvocation getActionInvocation() {
        return ActionContext.getContext().getActionInvocation();
    }

    public boolean isAjaxRequest() {
        return MethodPropertiesUtil.isAjaxRequest(getActionInvocation());
    }

    public boolean isAjaxDivPopupRequest() {
        return isAjaxRequest() && !isEmpty(getAjaxDivPopupId());
    }

    public String getAjaxDivPopupId() {
        return ajaxDivPopupId;
    }

    public void setAjaxDivPopupId(String ajaxDivPopupId) {
        this.ajaxDivPopupId = ajaxDivPopupId;
    }

    public Object getMenuResource() {
        return null;
    }

    public Object getSubMenuResource() {
        return null;
    }

    public Object getNestedSubMenuResource() {
        return null;
    }

    /**
     * The validation handler for all network actions.  It passes all validation errors
     * on to struts.
     */
    private class NetworkActionValidationHandler extends BaseValidationHandler {

        private final NetworkAction networkAction;

        public NetworkActionValidationHandler(NetworkAction networkAction) {
            this.networkAction = networkAction;
        }

        public final void addWordletizedActionError(String anErrorMessage, Object... args) {
            networkAction.addActionError(getText(anErrorMessage, args));
        }

        public final void addWordletizedFieldError(String fieldName, String errorMessage, Object... args) {
            networkAction.addFieldError(fieldName, getText(errorMessage, args));
        }

        @Override
        public final void addWordletizedRequiredFieldError(String fieldName, String fieldWordletName, Object... args) {
            super.addRequiredFieldError(fieldName, wordlet(fieldWordletName, args));
        }

        @Override
        public final void addRequiredFieldError(String fieldName, String fieldLabel) {
            if (!getNetworkContext().getReqResp().isPost()) {
                addWordletizedFieldError(fieldName, "required.field", fieldLabel);
                return;
            }
            super.addRequiredFieldError(fieldName, fieldLabel);
        }

        @Override
        public void addActionError(String anErrorMessage) {
            networkAction.addActionError(anErrorMessage);
        }

        @Override
        public void addFieldError(String fieldName, String errorMessage) {
            networkAction.addFieldError(fieldName, errorMessage);
        }

        public String getText(String wordlet, Object... args) {
            if (isEmptyOrNull(args)) {
                return networkAction.getText(wordlet);
            }
            return networkAction.getText(wordlet, Arrays.asList(args));
        }

        public boolean hasErrors() {
            return networkAction.hasErrors();
        }

        public List<ValidationError> getValidationErrors() {
            return Collections.singletonList(new ValidationError("There were validation errors, but they should have been handled by the action."));
        }

        public boolean isThrowApplicationErrorOnValidationError() {
            return false;
        }
    }
}
