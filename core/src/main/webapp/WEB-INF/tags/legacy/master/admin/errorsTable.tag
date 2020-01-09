<%@ tag import="org.narrative.network.core.cluster.actions.server.*" %>
<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="errorInfos" required="true" type="java.util.Collection" %>
<%@ attribute name="errorType" required="true" type="java.lang.Object" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.ClusterAction"/>

<g:set var="errorType" object="${errorType}" className="org.narrative.network.core.cluster.actions.server.ErrorStatusAction.ErrorType" />

<gct:doOnce id="removeErrorRowFunction">
    <g:set var="clearAllParam" object="<%=ClearErrorStatusAction.CLEAR_ALL_PARAM%>" className="java.lang.String" />
    <script type="text/javascript">
        function removeErrorRow(rowId, hashcode, errorType) {
            var el = $(rowId);
            el.parentNode.removeChild(el);

            clearErrorOnServer(hashcode, errorType, false);
        }

        clearErrorOnServer = function(hashcode, errorType, clearAll) {
            $('clearErrorStackTraceHashcodeField').value = hashcode;
            $('clearErrorErrorTypeField').value = errorType;
            $('clearAllErrorsField').value = clearAll ? 'true' : 'false';

            clearErrorCallAjax();
        };

        onClearErrorSuccess = function(xml, text, queryParams) {
            var clearAll = queryParams['${clearAllParam}'];
            if (!isEmpty(clearAll) && clearAll == 'true') {
                window.location.reload();
            }
        };
    </script>

    <g:set var="clearErrorActionPath" object="<%=ClearErrorStatusAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <gfs:ajaxForm id="clearError" action="${clearErrorActionPath}!execute" isGet="${false}" onAjaxSuccessFunction="onClearErrorSuccess">
        <gfs:hidden id="clearErrorErrorTypeField" name="<%=ClearErrorStatusAction.ERROR_TYPE_PARAM%>" value="" />
        <gfs:hidden id="clearErrorStackTraceHashcodeField" name="<%=ClearErrorStatusAction.STACK_TRACE_HASHCODE_PARAM%>" value="" />
        <gfs:hidden id="clearAllErrorsField" name="${clearAllParam}" value="" />
    </gfs:ajaxForm>
</gct:doOnce>

<b>${errorType.title}</b>
<span class="smallfont">(<a href="javascript:void(0);" onclick="clearErrorOnServer(null, '${errorType}', true);">Clear All</a>)</span>
<table>
    <thead>
        <tr>
            <td>&nbsp;</td>
            <c:if test="${not errorType.ajaxError}">
                <td>[Type]</td>
            </c:if>
            <td>[Count]</td>
            <c:if test="${not errorType.ajaxError}">
                <td>[SpiderCount]</td>
                <td>[FilterCount]</td>
            </c:if>
            <td width="400">[Message]</td>
            <td>[Stack Hashcode]</td>
            <c:if test="${not errorType.ajaxError}">
                <td>[Exception Class Hashcode]</td>
            </c:if>
            <td>[StackTrace]</td>
        </tr>
    </thead>
    <tbody>
        <g:forEach items="${errorInfos}" obj="errorInfo" className="org.narrative.network.core.statistics.ErrorInfo">
            <g:set var="exceptionInfo" object="${errorType.ajaxError ? null : errorInfo}" className="org.narrative.network.core.statistics.ExceptionInfo" />
            <g:set var="ajaxErrorInfo" object="${errorType.ajaxError ? errorInfo : null}" className="org.narrative.network.core.statistics.AjaxErrorInfo" />
            <g:set var="rowId" object="${g:seq()}" />
            <tr id="errorRow${rowId}">
                <td valign="top"><a href="javascript:void(0);" onclick="removeErrorRow('errorRow${rowId}', '${errorInfo.stackTraceHashcode}', '${errorType}');"><gn:staticImage src="/images/cluster/trashcan18px.png" altTitle="Remove from this page (doesn't remove from server, so error will be visible again on refresh)" /></a></td>
                <c:if test="${not errorType.ajaxError}">
                    <td valign="top">${g:classSimpleName(exceptionInfo.rootCauseExceptionClass)}</td>
                </c:if>
                <td valign="top">
                    <c:choose>
                        <c:when test="${errorInfo.count==exceptionInfo.spiderCount}">${errorInfo.count}</c:when>
                        <c:otherwise><b>${errorInfo.count}</b></c:otherwise>
                    </c:choose>
                </td>
                <c:if test="${not errorType.ajaxError}">
                    <td valign="top">${exceptionInfo.spiderCount}</td>
                    <td valign="top">
                        <c:choose>
                            <c:when test="${exceptionInfo.bubbledToFilterCount==0}">0</c:when>
                            <c:otherwise><b>${exceptionInfo.bubbledToFilterCount}</b></c:otherwise>
                        </c:choose>
                    </td>
                </c:if>
                <td valign="top" width="400">${g:elipse(fn:escapeXml(errorInfo.errorMessage), 250)}</td>
                <td valign="top"><nobr>${errorInfo.stackTraceHashcode}</nobr></td>
                <c:if test="${not errorType.ajaxError}">
                    <td valign="top"><nobr>${exceptionInfo.rootCauseHashcode}</nobr></td>
                </c:if>
                <td valign="top"><a href="javascript:void(0);" onclick="viewStackTrace('${errorInfo.stackTraceHashcode}', '${exceptionInfo.rootCauseHashcode}', this);">View</a></td>
            </tr>
        </g:forEach>
    </tbody>
</table>
