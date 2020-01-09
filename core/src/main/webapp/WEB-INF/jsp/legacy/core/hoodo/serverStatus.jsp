<%--
  Created by IntelliJ IDEA.
  User: paul
  Date: Aug 30, 2006
  Time: 3:40:05 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.ServerStatusAction"/>

<table>
    <tr>
        <td>
            <gma:serverStatusDetailsTable serverStatus="${action.serverStatus}"/>
        </td>
        <td>
            <gma:serverStatusStatsTable serverStatus="${action.serverStatus}" />
        </td>
    </tr>
</table>