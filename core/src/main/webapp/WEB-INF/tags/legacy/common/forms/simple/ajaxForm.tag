<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="isGet" type="java.lang.Boolean" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="name" %>
<%--<%@ attribute name="isFileUpload" type="java.lang.Boolean" %>--%>
<%@ attribute name="onsubmit" %>
<%@ attribute name="displayFormInline" %>
<%@ attribute name="divForAjaxReplace" description="Div for replacement after ajax call" %>
<%@ attribute name="objForAjaxReplace" description="Object for replacement after ajax call" %>
<%@ attribute name="onAjaxSuccessFunction" description="Function to call after successful ajax response, will pass XML dom if exists" %>
<%@ attribute name="onAjaxErrorsFunction" description="Function to call after successful ajax failure" %>
<%@ attribute name="reloadPageOnSuccess" description="Set to true if you want to reload the current page on successful submission of the ajax request. In order for this to work, the ajax action will have to return an ajaxReloadResponse.  Note that if you are executing an ajax request inside of an iframe, reloadPageOnSuccess means that the _parent_ window will be reloaded on success." %>
<%@ attribute name="useRedirectForReload" description="Set to true if  using reloadPageOnSuccess and you want to redirect to the action's redirect on completion instead of just reloading the page." %>
<%@ attribute name="showPleaseWaitOnSubmit" type="java.lang.Boolean" description="Controls whether the pleaseWait popup window should appear upon submission of the form.  Useful for forms you wouldn't want submitted twice since it will disable the page.  Will automatically hide in the event of ajax errors." %>
<%@ attribute name="dontHidePleaseWaitOnSuccess" type="java.lang.Boolean" description="Prevents the please wait popup window from hiding on success.  Useful for places like reply where the on success handler will redirect the user to a new page, so we want the please wait message to persist on the page until the page changes." %>
<%@ attribute name="insertErrorIntoReplaceDiv" type="java.lang.Boolean" description="Controls whether any unexpected errors will be inserted into the replace div.  Defaults to false." %>
<%@ attribute name="suppressErrorMessage" type="java.lang.Boolean" description="Controls whether any unexpected errors will be displayed to the user.  Defaults to false so that all unexpected errors will be displayed.  Should be disabled for things like auto-tagging and any other 'transparent' ajax actions" %>
<%@ attribute name="formErrorMessagesId" description="Some ajax forms are never visually displayed on the page.  Thus, for those forms, inserting the error messages directly into the form doesn't make much sense.  An element id can be specified in this parameter to specify an alternate element to insert error messages into." %>
<%@ attribute name="skipFormResetOnSuccess" description="By default, the ajax form will be reset on success.  Set this to true to bypass that behavior.  Useful on the reply form where we don't want to reset the form since we'll just be doing an ajax redirect on success." %>
<%@ attribute name="usesReCaptcha" type="java.lang.Boolean" description="Set to true if this form uses reCaptcha, in which case the reCaptcha header javascript configuration should be included." %>
<%@ attribute name="warningMessagesIncludedExternally" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="sss" uri="http://www.narrative.org/tags/struts"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />

<%-- bl: the primary purpose of ajaxForm.tag is to hard code some of the common parameters for gfs:form
     and also to enforce that other parameters that aren't always required on gfs:form are actually supplied
     always for ajax requests.  the best example of this is the submit button.  we need the submit button
     in order to do our expired session detection in ajax requests and for the popup window framework that
     will automatically renew your session and resubmit the ajax request. --%>

<%-- bl: this uniqueFormId needs to exactly match the formId logic in form.tag. --%>
<g:set var="uniqueFormId" object="${not empty id ? id : sss:actionName(action)}" />
<g:set var="divForAjaxReplaceId">${uniqueFormId}DivForAjaxReplace</g:set>
<g:set var="needsSuccessHandler" object="${not empty onAjaxSuccessFunction or (showPleaseWaitOnSubmit and not dontHidePleaseWaitOnSuccess and not reloadPageOnSuccess)}" />
<g:set var="needsErrorHandler" object="${not empty onAjaxErrorsFunction or showPleaseWaitOnSubmit}" />

<script type="text/javascript">
    ${divForAjaxReplaceId} = ${g:getJavascriptStringValue(divForAjaxReplace)};
    
    <c:if test="${needsSuccessHandler}">
    ${uniqueFormId}SuccessHandler = function(xml, text, queryParams) {
        <c:if test="${not empty onAjaxSuccessFunction}">
        ${onAjaxSuccessFunction}.apply(this, arguments);
        </c:if>
        <c:if test="${showPleaseWaitOnSubmit and not dontHidePleaseWaitOnSuccess and not reloadPageOnSuccess}">
        pleaseWaitPopupWindow.hide();
        </c:if>
    };
    </c:if>
            
    <c:if test="${needsErrorHandler}">
    ${uniqueFormId}ErrorHandler = function() {
        <c:if test="${not empty onAjaxErrorsFunction}">
        ${onAjaxErrorsFunction}();
        </c:if>
        <c:if test="${showPleaseWaitOnSubmit}">
        pleaseWaitPopupWindow.hide();
        </c:if>
    };
    </c:if>
    
    ${uniqueFormId}OnSubmitHandler = function(formObj,overrideReplaceElt,extraOnSuccessHandler) {
        ${onsubmit};
        <c:if test="${showPleaseWaitOnSubmit}">
        pleaseWaitPopupWindow.show();
        </c:if>
        var defaultSuccessHandler = ${needsSuccessHandler ? g:concat(uniqueFormId, 'SuccessHandler') : 'null'};
        var successHandler = function(xml, text, queryParams) {
            if(defaultSuccessHandler) {
                defaultSuccessHandler.apply(this, arguments);
            }
            if(extraOnSuccessHandler) {
                extraOnSuccessHandler.apply(this, arguments);
            }
        };
        ajaxFormSubmit(formObj,overrideReplaceElt?overrideReplaceElt:${not empty objForAjaxReplace ? objForAjaxReplace : divForAjaxReplaceId},successHandler,${needsErrorHandler ? g:concat(uniqueFormId, 'ErrorHandler') : 'null'}, ${needsErrorHandler ? g:concat(uniqueFormId, 'ErrorHandler') : 'null'}, ${insertErrorIntoReplaceDiv==null ? false : insertErrorIntoReplaceDiv}, ${suppressErrorMessage==null ? false : suppressErrorMessage}, ${g:getJavascriptStringValue(formErrorMessagesId)}, ${skipFormResetOnSuccess ? true : false});
    };
    
    <c:if test="${not empty id}">
    ${id}CallAjax = function(overrideReplaceElt,extraOnSuccessHandler) {
        ${uniqueFormId}OnSubmitHandler($('${id}'), overrideReplaceElt, extraOnSuccessHandler);
    };
    </c:if>

    ${uniqueFormId}HandleSubmit = function(form) {
        <%-- bl: need the behavior of the OnSubmitHandler to be fault-tolerant.  if an error occurs, we want to catch the error
             to prevent the exception from stopping us from returning false in the onsubmit handler.  the return false ensures
             that the normal form submit behavior is stopped.  in the event of an exception, the normal submit behavior will take place
             in which case there may be issues with the data submitted to the server and the response page returned to the user
             will be just the contents of the AJAX response, if any.  bad user experience, especially since the result of the form
             submit is undefined since we have no way of knowing at what point the failure occurred. --%>
        try {
            ${uniqueFormId}OnSubmitHandler(form);
        } catch(e) {
            try {
                var errorMessage = Narrative.ajaxExceptionErrorMessage + '-1';
                if(Narrative.isDevOrQaServer) {
                    errorMessage += '<br /><br /><b>The following message will only appear on QA servers.  It will never appear on a production environment.</b><br /><br />';
                    errorMessage += '<b>action</b>: ${g:escapeJavascriptLiteralString(action,false)}<br /><b>Debug</b>: onPreAjaxException';
                    errorMessage += '<br /><b>exception</b>: ' + e;
                    errorMessage += '<br /><b>stack</b>: ' + Narrative.getStackTrace(e);
                    errorMessage += '<br /><b>exception properties</b>: ' + debugHashProperties($H(e), true);
                }
                showAjaxErrorMessage(Narrative.ajaxFailureErrorTitle, errorMessage);
            } catch(e2) {
                var alertErrorMessage = Narrative.ajaxExceptionErrorMessage + '-2';
                if(Narrative.isDevOrQaServer) {
                    alertErrorMessage += '\nThe following message will only appear on QA servers.  It will never appear on a production environment.\n\n';
                    alertErrorMessage += 'action: ${g:escapeJavascriptLiteralString(action,false)}\nDebug: onPreAjaxException2';
                    alertErrorMessage += '\nexception: ' + e;
                    alertErrorMessage += '\nstack: ' + Narrative.getStackTracePlainText(e);
                    alertErrorMessage += '\nexception properties: ' + debugHashProperties($H(e));
                    alertErrorMessage += '\nexception2: ' + e2;
                    alertErrorMessage += '\nstack2: ' + Narrative.getStackTracePlainText(e2);
                    alertErrorMessage += '\nexception properties: ' + debugHashProperties($H(e2));
                }
                <%-- bl: if all else fails, use an alert as the error message. --%>
                alert(alertErrorMessage);
            }
        }
    };
</script>
<g:set var="isAjaxFormOriginal" object="${isAjaxForm}" />
<g:set var="isAjaxForm" object="${true}" scope="request" />

<gfs:form action="${action}"
          method="${isGet ? 'get' : 'post'}"
          name="${name}"
          id="${id}"
          onsubmit="${uniqueFormId}HandleSubmit(this);return false;"
          cssStyle="${displayFormInline ? 'display:inline;' : ''}">
          <%--isMultipart="${isFileUpload}"
          target="${isFileUpload ? g:concat(uniqueFormId, 'IFrame') : ''}"--%>
    <c:if test="${reloadPageOnSuccess}">
        <%-- bl: set the redirect to whatever the current url is to ensure the user is returned back to 
             the proper page.  unfortunately ajax requests do not include the referrer in the URL. --%>
        <gfs:hidden name="redirect" value="${useRedirectForReload ? networkAction.redirect : null}" id="${uniqueFormId}RedirectParamForReload" />
        <script type="text/javascript" language="JavaScript">
            <%-- bl: reloadPageOnSuccess will reload the parent window if the ajax form
                 is being submitted in an iframe. --%>
            if(isEmpty($('${uniqueFormId}RedirectParamForReload').value)) {
                var windowToUse = getIframeParent();
                $('${uniqueFormId}RedirectParamForReload').value = windowToUse.document.location.href;
            }
        </script>
    </c:if>

    <c:if test="${not warningMessagesIncludedExternally}">
        <gfs:formErrorMessages formId="${uniqueFormId}" cssStyle="display:none;" />
    </c:if>

    <jsp:doBody />
</gfs:form>

<c:if test="${usesReCaptcha}">
    <script type="text/javascript">
        $('${uniqueFormId}').reloadCaptcha = function() {
            Recaptcha.create("${networkRegistry.reCaptchaPublicKey}",
                "reCaptchaDiv${uniqueFormId}",
                {
                    theme: "white"
                }
            );
        };
        $LAB.script('${networkAction.networkContext.useSecureUrls ? 'https' : 'http'}://www.google.com/recaptcha/api/js/recaptcha_ajax.js').wait(function() {
            $('${uniqueFormId}').reloadCaptcha();
        });
    </script>
</c:if>

<g:set var="isAjaxForm" object="${isAjaxFormOriginal}" scope="request" />

<%--<c:if test="${isFileUpload}">
    <script type="text/javascript">
        function ${uniqueFormId}IFrameNotifier(){
            <c:choose>
                <c:when test="${not empty objForAjaxReplace}">
                    Element.update(${objForAjaxReplace}, $('${uniqueFormId}IFrameId').innerHTML);
                </c:when>
                <c:when test="${not empty divForAjaxReplace}">
                     // todo: get this working when needed
                     Element.update(${g:getJavascriptStringValue(divForAjaxReplace)}, $('${uniqueFormId}IFrameId').innerHTML);
                </c:when>
            </c:choose>
            <c:if test="${not empty onAjaxSuccessFunction}">
                 ${onAjaxSuccessFunction}('${uniqueFormId}IFrameId');
            </c:if>
        }
    </script>
</c:if>--%>

<%-- bl: this previously was being sent back in the response for the ajax file upload in order to trigger
     the iframe notifier function.
<script type="text/javascript">
    setTimeout(function(){eval('window.parent.'+this.name+'Notifier();');},0);
</script>
--%>