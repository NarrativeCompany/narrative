<%--
  Created by IntelliJ IDEA.
  User: barry
  Date: Dec 13, 2005
  Time: 11:53:41 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.RecentProcessOutliersAjaxAction" />

<h3>${action.processName}</h3>

<g:forEach items="${action.recentSlowActionInfo}" obj="slowRequestDetails" className="org.narrative.common.util.processes.RequestProcessHistory.SlowRequestDetails">
    ${slowRequestDetails.processRunningTime}ms - ${g:disableHtml(slowRequestDetails.logInfo)}<br /><br />
</g:forEach>