<%@ tag import="org.narrative.network.shared.services.*"%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="confirmationMessageId" required="true" description="An ID to give to this confirmation message dialog." %>
<%@ attribute name="title" required="true" description="The title for the confirmation message window." %>
<%@ attribute name="message" required="true" description="The message for the confirmation message window." %>
<%@ attribute name="showNow" required="false" description="Whether or not the dialog should be shown immediately.  Defaults to true." %>
<%@ attribute name="useDefaultAutoHideDelayMs" required="false" description="Whether or not the default auto-hide delay should be used for this confirmation message." %>
<%@ attribute name="autoHideDelayMs" required="false" description="The number of milliseconds after which this confirmation message should disappear." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />

<g:set var="autoHideDelayMs">
    <c:choose>
        <c:when test="${useDefaultAutoHideDelayMs}"><%=ConfirmationMessage.DEFAULT_AUTO_HIDE_DELAY_MS%></c:when>
        <c:when test="${not empty autoHideDelayMs}">${autoHideDelayMs}</c:when>
    </c:choose>
</g:set>

<g:set var="isShowingNow" object="${showNow==null or showNow}" />

<script type="text/javascript">
    ${confirmationMessageId}Dialog = createConfirmationMessage('${g:escapeJavascriptLiteralString(title, false)}', '${g:escapeJavascriptLiteralString(message, false)}', {
        autoHideDelayMs : ${not empty autoHideDelayMs ? autoHideDelayMs : 'null'},
        isShowingNow : ${isShowingNow ? true : false}
    });
    <c:if test="${isShowingNow}">
        <%-- bl: just like with div popups, we can't show these dialogs immediately or else they will break the DOM
             in IE and will result in "Operation unsupported" errors and the page load completely failing. thus,
             for IE, we need to open the dialog only once the page has fully rendered. --%>
        <c:choose>
            <c:when test="${networkContext.reqResp.clientAgentInformation.clientAgentType.internetExplorer}">
        addOnloadHandler(function() {
            ${confirmationMessageId}Dialog.openDialog();
        });
            </c:when>
            <c:otherwise>
        ${confirmationMessageId}Dialog.openDialog();
            </c:otherwise>
        </c:choose>
    </c:if>
</script>
