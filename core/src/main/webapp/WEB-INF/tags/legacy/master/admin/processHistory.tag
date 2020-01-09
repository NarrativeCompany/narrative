<%--
  User: brian
  Date: Jun 3, 2009
  Time: 8:34:37 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="allHistory" required="true" type="java.util.Collection" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<%-- bl: this tag may not always be used in the context of an action, so we can't use NetworkAction here. --%>
<g:ref var="contextHolder" className="org.narrative.network.shared.context.NetworkContextHolder" />

<h3>Root Process History</h3>
<table>
    <tr>
        <th>[Name]</th>
        <th>[Type]</th>
        <th>[Num Req]</th>
        <th>[own ms/req]</th>
        <th>[own total ms]</th>
        <th>[ms/req]</th>
        <th>[total ms]</th>
    </tr>
    <g:forEach items="${allHistory}" obj="history" className="org.narrative.common.util.processes.GenericHistory">
        <c:if test="${history.forRootProcess}">
            <tr>
                <td>${g:elipse(history.name, 100)}</td>
                <td>${history.processType}</td>
                <td>[${history.allRequestInfo.numberOfRequests}]</td>
                <td>[${history.allRequestInfo.averageOwnMSPerRequest}]</td>
                <td>[${history.allRequestInfo.ownRequestRunningTimeMS}]</td>
                <td>[${history.allRequestInfo.averageTotalMSPerRequest}]</td>
                <td>[${history.allRequestInfo.totalRequestTimeMS}]</td>
            </tr>
        </c:if>
    </g:forEach>
</table>
<br/>

<h3>Sub-Process History</h3>
<table>
    <tr>
        <th>[Name]</th>
        <th>[Type]</th>
        <th>[Num Req]</th>
        <th>[own ms/req]</th>
        <th>[own total ms]</th>
        <th>[ms/req]</th>
        <th>[total ms]</th>
        <th>[outliers ms/req]</th>
        <th>[outliers reqs]</th>
    </tr>
    <g:forEach items="${allHistory}" obj="history" className="org.narrative.common.util.processes.GenericHistory">
        <c:if test="${not history.forRootProcess}">
            <tr>
                <td>${g:elipse(history.name, 100)}</td>
                <td>${history.processType}</td>
                <td>[${history.allRequestInfo.numberOfRequests}]</td>
                <td>[${history.allRequestInfo.averageOwnMSPerRequest}]</td>
                <td>[${history.allRequestInfo.ownRequestRunningTimeMS}]</td>
                <td>[${history.allRequestInfo.averageTotalMSPerRequest}]</td>
                <td>[${history.allRequestInfo.totalRequestTimeMS}]</td>
                <td>[${history.outlierRequestInfo.averageTotalMSPerRequest}]</td>
                <td>
                    <c:choose>
                        <c:when test="${history.outlierRequestInfo.numberOfRequests>0}">
                            <a href="javascript:void(0);" onclick="showRecentOutliersPopup(this, '${g:escapeQuotes(g:escapeJavascriptLiteralString(history.name, false))}');">${history.outlierRequestInfo.numberOfRequests}</a>
                        </c:when>
                        <c:otherwise>${history.outlierRequestInfo.numberOfRequests}</c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </g:forEach>
</table>

<script type="text/javascript">
    showRecentOutliersPopup = function(linkObj, processName) {
        recentOutlierRequestsAjaxPopup.show(linkObj, '/processName/' + urlEncodePathElement(processName));
    };
</script>

<gct:ajaxDivPopup id="recentOutlierRequests" title="Recent Outlier Requests" url="/recentProcessOutliers" width="800" height="500" isResizable="true" />