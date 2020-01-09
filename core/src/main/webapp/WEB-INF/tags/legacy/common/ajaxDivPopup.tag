<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true"  %>
<%@ attribute name="style" required="false"  %>
<%@ attribute name="cssClass" required="false"  %>
<%@ attribute name="url" required="false" type="java.lang.String" description="The URL for the ajax request. Required if ajaxFormId not present." %>
<%@ attribute name="ajaxFormId" required="false" type="java.lang.String" description="The ajax form to submit instead of using a GET url.  Required if url not present." %>
<%@ attribute name="isModal" required="false" type="java.lang.Boolean" description="Determines whether the background behind the window will be disabled.  Defaults to true." %>
<%@ attribute name="isResizable" required="false" type="java.lang.Boolean" description="Determines whether the window is resizable.  Defaults to false." %>
<%@ attribute name="onCloseJavascript" required="false" type="java.lang.String" description="Javascript to execute when the window is closed." %>
<%@ attribute name="onLoadJavascript" required="false" type="java.lang.String" description="Javascript to execute when the window is loaded." %>

<%@ attribute name="onlyDestroyOnFullReload" required="false" type="java.lang.String" description="If this popup is contained within an AJAX div popup, then it will be destroyed automatically any time that the AJAX div popup's contents are reloaded.  In certain circumstances, you may want to manually clear the popup's associated items when doing a partial reload.  In those cases, set this to true." %>

<%@ attribute name="initJavascriptBlock" required="false" description="Any javascript to execute on window initialization.  Most useful for adding buttons to the popup window, which must happen prior to window rendering.  Must be enclosed inside of a script tag or else the javascript code will just be displayed on the page." %>

<%@ attribute name="height" type="java.lang.Integer" %>
<%@ attribute name="width" type="java.lang.Integer" %>
<%@ attribute name="setFocusOnId" required="false" type="java.lang.String" description="The id of the element to focus() when the popup displays." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<gct:doOnce id="ajaxDivPopup${id}">
    <gct:divPopup
            id="${id}"
            title="${title}"
            style="${style}"
            cssClass="${cssClass}"
            isModal="${isModal==null ? true : isModal}"
            isResizable="${isResizable}"
            onCloseJavascript="$(${id}PopupWindow.popupContentDivId).innerHTML='';${onCloseJavascript}"
            width="${width}"
            height="${height}"
            onlyDestroyOnFullReload="${onlyDestroyOnFullReload}"
            initJavascriptBlock="${initJavascriptBlock}" />
    <%-- bl: add the ajaxPopupDiv param to the URL so that we know in the request that this is an ajax div popup request. --%>
    <g:url value="${url}" appendParamsAsPathParams="true" id="newUrl">
        <g:param name="ajaxDivPopupId" value="${id}" />
    </g:url>
    <script type="text/javascript" language="JavaScript">
    if(typeof ${id}AjaxPopup == 'undefined' || ${id}AjaxPopup==null) {
        ${id}AjaxPopup = {
            url : '${not empty ajaxFormId ? '' : g:escapeJavascriptLiteralString(newUrl,false)}',
            defaultPleaseWaitText : '${gn:wordlet('tags.common.ajaxDivPopup.pleaseWait')}',
            pleaseWaitText : null,
            urlToPrepend : null,
            callAjax : function(extraUrlToAppend) {
                var urlToAppend = '';
                if(!isEmpty(this.urlToAppend)) {
                    urlToAppend += this.urlToAppend;
                }
                if(!isEmpty(extraUrlToAppend)) {
                    urlToAppend += extraUrlToAppend;
                }
                <c:choose>
                    <c:when test="${not empty ajaxFormId}">
                <%-- bl: pre-populate the url field to the initial value of the form's action so that we don't
                     continually append the ajaxDivPopupId and other parameters to the URL on each submission. --%>
                if(isEmpty(this.url)) {
                    this.url = $('${ajaxFormId}').action;
                }
                var formActionToUse = this.url + '/ajaxDivPopupId/${id}';
                $('${ajaxFormId}').action = isEmpty(urlToAppend) ? formActionToUse : formActionToUse + urlToAppend;
                ${ajaxFormId}CallAjax(${id}PopupWindow.popupContentDivId, this.onload);
                    </c:when>
                    <c:otherwise>
                var urlToUse = isEmpty(urlToAppend) ? this.url : (this.url + urlToAppend);
                if(!isEmpty(this.urlToPrepend)) {
                    urlToUse = this.urlToPrepend + urlToUse;
                }
                defaultAjaxSubmit(urlToUse, ${id}PopupWindow.popupContentDivId, this.onload);
                    </c:otherwise>
                </c:choose>
            },
            showPleaseWait : function() {
                $(${id}PopupWindow.popupContentDivId).innerHTML = this.pleaseWaitText==null ? this.defaultPleaseWaitText : this.pleaseWaitText;
            },
            show : function(linkObj, urlToAppend) {
                this.urlToAppend = urlToAppend;
                ${id}PopupWindow.show(linkObj);
            },
            reload : function(urlToAppend,skipPleaseWait) {
                this.urlToAppend = urlToAppend;
                this.reloadInternal(null, skipPleaseWait);
            },
            reloadInternal : function(extraUrlToAppend,skipPleaseWait) {
                // clear out any divPopups that may have been included in the AJAX popup, too.
                this.cleanupAjaxItems(true);
                if(!skipPleaseWait) {
                    this.showPleaseWait();
                }
                this.callAjax(extraUrlToAppend);
            },
            reloadCurrentUrl : function(extraUrlToAppend,skipPleaseWait) {
                this.reloadInternal(extraUrlToAppend,skipPleaseWait);
            },
            cleanupAjaxItems : function(isReloadingFullPopupContents) {
                cleanupAjaxDivPopupItems('${id}', isReloadingFullPopupContents);
            },
            hide : function() {
                ${id}PopupWindow.hide();
            },
            onload : function() {
                <%-- bl: adding a 50ms delay to centering the AJAX popup windows. apparently there is an issue with a race
                     condition between updating the body of the popup and calling the onload here. in order to ensure
                     that the body has loaded, this delay will ensure the popup gets centered properly. --%>
                setTimeout(function() {
                    ${id}PopupWindow.center();
                    ${id}PopupWindow.setButtonsDisabled(false);
                    <%-- bl: add a slight delay to the focus call to make sure it works in all browsers.  IE in particular
                         had issues displaying the cursor immediately after the popup appeared. --%>
                    <c:if test="${not empty setFocusOnId}">
                        focusFormField('${setFocusOnId}');
                    </c:if>
                    ${onLoadJavascript}
                }, 50);
            },
            setTitle : function(title) {
                ${id}PopupWindow.setTitle(title);
            }
        };
    }

    if(!${id}PopupWindow.isInitialized()) {
        ${id}PopupWindow.onInitHandlers.push(function() {
            ${id}PopupWindow.on('beforeShowEvent', function() {
                ${id}AjaxPopup.cleanupAjaxItems(true);
                ${id}AjaxPopup.showPleaseWait();
                ${id}PopupWindow.setButtonsDisabled(true);
            });
            ${id}PopupWindow.on('showEvent', function() {
                ${id}AjaxPopup.callAjax();
            });
        });
    }
    </script>
</gct:doOnce>