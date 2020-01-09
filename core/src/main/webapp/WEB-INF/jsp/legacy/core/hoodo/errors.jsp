<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.ErrorStatusAction"/>
<gct:wrapperCommonParams title="Errors">
    <jsp:attribute name="extraHeadTag">
        <style type="text/css">
            <%-- bl: adding this so that we get scrollbars in the stack trace popup windows --%>
            .yui-panel .bd {
                overflow: auto !important;
            }
            pre {
                word-wrap: normal;
                overflow-x: scroll;
            }
        </style>
    </jsp:attribute>
</gct:wrapperCommonParams>
<ge:clusterWrapper useServletSpecificBaseUrl="${true}">
    <gma:serverToolbar isErrors="true"/>

    <script type="text/javascript" language="JavaScript">
        var stackTraces = {};
        <g:forEach items="${action.errorTypeToErrors}" obj="entry" className="java.util.Map.Entry">
            <g:set var="errorType" object="${entry.key}" className="org.narrative.network.core.cluster.actions.server.ErrorStatusAction.ErrorType" />
            <g:set var="errorInfos" object="${entry.value}" className="java.util.Collection" />
            <g:forEach items="${errorInfos}" obj="errorInfo" className="org.narrative.network.core.statistics.ErrorInfo">
        stackTraces['${errorInfo.stackTraceHashcode}'] = '';
                <g:forEach items="${errorInfo.requestInfos}" obj="requestInfo" className="java.lang.Object">
        stackTraces['${errorInfo.stackTraceHashcode}'] += '${g:escapeJavascriptLiteralString(errorType.ajaxError ? requestInfo : g:disableHtml(requestInfo),false)}<br /><br />';
                </g:forEach>
                <%-- bl: don't include the stack trace separately for AJAX errors since the stack trace is already included in each requestInfo. --%>
                <c:if test="${not errorType.ajaxError}">
        stackTraces['${errorInfo.stackTraceHashcode}'] += '${g:escapeJavascriptLiteralString(errorInfo.stackTrace,false)}';
                </c:if>
            </g:forEach>
        </g:forEach>
    </script>
    
    <script type="text/javascript" language="JavaScript">
        function viewStackTrace(hashcode, rootCauseHashcode, linkObj) {
            var message = '[' + hashcode + ']';
            if(rootCauseHashcode) {
                message += '[' + rootCauseHashcode + ']';
            }
            message += '${action.networkRegistry.version}-${action.networkRegistry.clusterId}.${action.networkRegistry.servletName}<br /><br />';
            message += stackTraces[hashcode];
            stackTracePopupPopupWindow.setContent(message);
            stackTracePopupPopupWindow.show(linkObj);
        }
    </script>
    
    <gct:divPopup id="stackTracePopup" title="Stack Trace" width="800" height="500" isResizable="true" />

    <script type="text/javascript">
        stackTracePopupPopupWindow.onOpenHandlers.push(function() {
            <%-- bl: reset the scrollTop value for the popup so that we don't open new windows scrolled half-way down,
                 which gets really annoying when looking through lots of different errors. --%>
            $(stackTracePopupPopupWindow.popupContentDivId).parentNode.scrollTop = 0;
        });
    </script>

    <g:forEach items="${action.errorTypeToErrors}" obj="entry" className="java.util.Map.Entry" varStatus="errorStatus">
        <g:set var="errorType" object="${entry.key}" className="org.narrative.network.core.cluster.actions.server.ErrorStatusAction.ErrorType" />
        <g:set var="errorInfos" object="${entry.value}" className="java.util.Collection" />
        <c:if test="${not errorStatus.first}">
            <br />
        </c:if>
        <gma:errorsTable errorInfos="${errorInfos}" errorType="${errorType}" />
    </g:forEach>

</ge:clusterWrapper>
