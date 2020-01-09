<%@ tag import="org.narrative.network.core.system.NetworkVersion" %><%--
  User: brian
  Date: Oct 10, 2008
  Time: 9:40:43 AM
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

<g:set var="networkVersion" object="<%=NetworkVersion.INSTANCE%>" className="org.narrative.network.core.system.NetworkVersion" />

<table nowrap="true" cellspacing="0">
    <tr>
        <td><b>Version:</b></td>
        <td>${action.networkRegistry.version}</td>
    </tr>
    <tr>
        <td><b>Jenkins Build:</b></td>
        <td>${networkVersion.jenkinsBuild}</td>
    </tr>
    <tr>
        <td><b>Branch:</b></td>
        <td>${networkVersion.branch}</td>
    </tr>
    <tr>
        <td><b>Git SHA:</b></td>
        <td>${networkVersion.gitSha}</td>
    </tr>
    <tr>
        <td><b>Startup datetime:</b></td>
        <td>${gn:dateFormatLongDatetime(g:getDateFromLong(serverStatus.servletStartTime))}</td>
    </tr>
    <tr>
        <td><b>Uptime:</b></td>
        <td>${gn:getElapsedTimeSinceFormatted(serverStatus.servletStartTime)} (${gn:formatNumber(serverStatus.uptimeMinutes)} mins)</td>
    </tr>

    <c:if test="${serverStatus.unixUptime != null}">
        <tr>
            <td><b>Server Load:</b></td>
            <td>${serverStatus.unixUptime}</td>
        </tr>
    </c:if>

    <tr>
        <td><b>Last HAPort Ping (HA ${serverStatus.heartbeatServerRunning?'':'not '}running):</b></td>
        <td>${serverStatus.lastHeartbeatServerPing==0 ? 'Never' : gn:getElapsedTimeSinceFormatted(serverStatus.lastHeartbeatServerPing)}</td>
        <td><a href="/toggleHeartbeatServer">Toggle Server</a></td>
    </tr>

</table>