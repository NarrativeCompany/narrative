<%@ tag import="org.narrative.common.util.IPDateUtil" %>
<%--
  User: brian
  Date: Oct 10, 2008
  Time: 9:39:21 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="serverStatus" required="true" type="org.narrative.network.core.cluster.actions.ServerStatusTask" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:set var="minutesInMS" object="<%=IPDateUtil.MINUTE_IN_MS%>" className="java.lang.Integer"/>
<table class="data_table" border="1" cellspacing="0">
    <tr>
        <th>&nbsp;</th>
        <th>Total</th>
        <th>Recent</th>
    </tr>
    <tr>
        <td><b>Response Time:</b></td>
        <td>${serverStatus.total.avgResponseTime}</td>
        <td>${serverStatus.recent.avgResponseTime}</td>
    </tr>
    <tr>
        <td><b>Request/Sec:</b></td>
        <td>${serverStatus.total.avgRequestsSec}</td>
        <td>${serverStatus.recent.avgRequestsSec}</td>
    </tr>
    <tr>
        <td><b>Reqests:</b></td>
        <td>${serverStatus.total.requests}</td>
        <td>${serverStatus.recent.requests}</td>
    </tr>

</table>
