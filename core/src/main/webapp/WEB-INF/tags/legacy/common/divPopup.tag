<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true"  %>
<%@ attribute name="style" required="false"  %>
<%@ attribute name="cssClass" required="false"  %>
<%@ attribute name="isVisible" required="false" type="java.lang.Boolean"  %>
<%@ attribute name="isModal" required="false" type="java.lang.Boolean" description="Determines whether the background behind the window will be disabled.  Defaults to true." %>
<%@ attribute name="isClosable" required="false" type="java.lang.Boolean" description="Determines whether the X displays for the window.  Defaults to true." %>
<%@ attribute name="isResizable" required="false" type="java.lang.Boolean" description="Determines whether the window is resizable.  Defaults to false." %>
<%@ attribute name="disableDrag" required="false" type="java.lang.Boolean" description="Determines whether the window is draggable.  Defaults to false (window is draggable)." %>
<%@ attribute name="onCloseJavascript" required="false" type="java.lang.String" description="Javascript to execute when the window is closed." %>
<%@ attribute name="onOpenJavascript" required="false" type="java.lang.String" description="Javascript to execute when the window is closed." %>

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
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<g:set var="previousDivPopupWindowId" object="${empty divPopupWindowId ? null : divPopupWindowId}" />
<g:set var="divPopupWindowId" object="${id}" scope="request" />

<c:if test="${isResizable and not (height>0)}">
    ${g:throwUnexpectedError('Should not use isResizable with an auto-height! Only use isResizable when a fixed height has been specified! Otherwise, any resize operation will remove auto-height.')}
</c:if>

<gct:doOnce id="divPopup${id}">
    <%-- bl: in order to prevent div id collisions if the same div is returned multiple times
         via separate AJAX requests or is included initially on the page and then also included
         via an AJAX request, include a seqOid when including the divs in the page body.  this way,
         we will never have duplicate div IDs and we also will only create the new PopupWindow
         object in the event that it doesn't already exist. --%>
    <g:set var="popupContentDivId" object="${id}${g:seq()}" />
    <script type="text/javascript">
    is${popupContentDivId}PopupWindowUndefined = typeof ${id}PopupWindow == 'undefined' || ${id}PopupWindow==null;
    <%-- bl: whew. OK. if the PopupWindow object is defined, but the underlying div that is supposed to be
         the source of the popup window no longer exists in the DOM, then we should destroy the old popup window
         and create a new popup window using the current object. this can happen, for example, with some of the content
         management popup windows on the AJAX replies list for a blog. note that this issue won't happen if the popup
         window is ever opened due to the fact that as soon as we initialize the popup window, we move the popup
         window's contents to the body of the page. in the case of the blog's AJAX replies, we actually were
         replacing the replies div via AJAX, which was destroying the underlying div body of the popup window,
         ultimately causing a JavaScript error. --%>
    if(!is${popupContentDivId}PopupWindowUndefined && !$(${id}PopupWindow.popupContentDivId)) {
        ${id}PopupWindow.destroy();
        <%-- bl: destroy the popup and then also destroy the PopupWindow variable
             so that the old popup is completed discarded and a new PopupWindow object will be created. --%>
        delete ${id}PopupWindow;
        is${popupContentDivId}PopupWindowUndefined = true;
    }
 	if(is${popupContentDivId}PopupWindowUndefined) {
        ${id}PopupWindow = createPopupWindow({
            id : '${id}',
            popupContentDivId : '${popupContentDivId}',
            title : '${g:escapeJavascriptLiteralString(title,false)}',
            resizable : ${isResizable ? true : false},
            modal : ${isModal==null or isModal},
            closable: ${isClosable==null ? true : isClosable},
            draggable: ${disableDrag ? false : true},
            ${not empty width ? g:concat3('width: ', width, ',') : ''}
            <%-- bl: strange, i know, but dividing the height by 2 here. Yahoo is doubling the height for some reason. --%>
            ${not empty height ? g:concat3('height: ', height/2, ',') : ''}
            onCloseFunction: function() {
                ${onCloseJavascript}
            }
        });
        
        <c:if test="${action.ajaxDivPopupRequest}">
        Narrative.ajaxDivPopupInnerDivPopups.registerInnerDivPopup('${action.ajaxDivPopupId}', ${id}PopupWindow, ${onlyDestroyOnFullReload ? true : false});
        </c:if>
    }
    </script> 

    ${initJavascriptBlock}
    <c:if test="${not empty setFocusOnId}">
    <script type="text/javascript">
        ${id}PopupWindow.onOpenHandlers.push(function() {
            <%-- bl: add a slight delay to the focus call to make sure it works in all browsers.  IE in particular
                 had issues displaying the cursor immediately after the popup appeared. --%>
            setTimeout(function() {
                focusFormField('${setFocusOnId}');
            }, 20);
        });
    </script>
    </c:if>
    <c:if test="${not empty onOpenJavascript}">
    <script type="text/javascript">
        ${id}PopupWindow.onOpenHandlers.push(function() {
            ${onOpenJavascript}
        });
    </script>
    </c:if>

    <div id="${popupContentDivId}PopupContentWrapper" style="display:none;" class="div_popup ${cssClass}" ${g:condAttr('style',style)}>
        <div id="${popupContentDivId}PopupHeader" class="hd do_rounded_div_css_ffsafari_tl do_rounded_div_css_ffsafari_tr"></div>
        <div id="${popupContentDivId}PopupContent" class="bd" ${g:condAttr('style',style)}>
            <jsp:doBody/>
        </div>
        <div id="${popupContentDivId}PopupFooter" class="ft"></div>
    </div>
    
    <script type="text/javascript">
        <%-- bl: this will prevent duplicate popup window divs from being on the page at all, since in this case
             a different popup content wrapper div will be used.  this is useful to prevent divs with the same
             ID from being included on the page when the divPopups are returned via AJAX requests (in which case
             they may be requested multiple times). --%>
        if(!is${popupContentDivId}PopupWindowUndefined) {
            var popupWindowEltToRemove = $('${popupContentDivId}PopupContentWrapper');
            popupWindowEltToRemove.parentNode.removeChild(popupWindowEltToRemove);
        }
        <%-- bl: the whole "isVisible" concept causes problems with onInitHandlers when loading pages inline now.
             onInitHandlers can not be added by calling code until after the popup window is shown, which is too late.
             thus, we will always show the popup window in an onload handler for all browsers (as opposed to just IE, as previously). --%>
        <%--<g:set var="showNow" object="${isVisible and width>0 and height>0 and not networkContext.reqResp.clientAgentInformation.clientAgentType.internetExplorer}" />
        <c:choose>
            <c:when test="${showNow}">
                ${id}PopupWindow.show();
            </c:when>
            <c:otherwise>
                --%><%-- bl: changed to set the content onload so that the page will have fully rendered, which is required in order
                     to accurately calculate the size of the window for auto-resizing purposes.  additionally, need to call show
                     onload in order to ensure that the page has loaded and the window library will properly determine the full
                     size of the window to place the overlay over. --%><%--
                <c:if test="${isVisible}">
                addOnloadHandler(function() {
                    ${id}PopupWindow.show();
                });
                </c:if>
            </c:otherwise>
        </c:choose>--%>
    </script>
</gct:doOnce>

<g:set var="divPopupWindowId" object="${previousDivPopupWindowId}" scope="request" />