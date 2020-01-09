<?xml version="1.0" encoding="UTF-8"?>
<%--
  Created by IntelliJ IDEA.
  User: paul
  Date: Aug 30, 2006
  Time: 2:15:51 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/xml;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.ServerStatusAction"/>
<server-info>
    <servlet>${action.networkRegistry.servletName}</servlet>
    <cluster-domain>${action.networkRegistry.clusterCpDomain}</cluster-domain>
    <cluster-cp-domain>${action.networkRegistry.clusterCpDomain}</cluster-cp-domain>
    <cluster-id>${action.networkRegistry.clusterId}</cluster-id>
    <avg-response-time>${action.serverStatus.total.avgResponseTime}</avg-response-time>
    <avg-request-per-second>${action.serverStatus.total.avgRequestsSec}</avg-request-per-second>
    <total-requests>${action.serverStatus.total.requests}</total-requests>
    <total-request-time>${action.serverStatus.total.requestTime}</total-request-time>
    <servlet-starttime>${action.serverStatus.servletStartTime}</servlet-starttime>
    <uptime-minutes>${action.serverStatus.uptimeMinutes}</uptime-minutes>
    <version>${action.networkRegistry.version}</version>
</server-info>
