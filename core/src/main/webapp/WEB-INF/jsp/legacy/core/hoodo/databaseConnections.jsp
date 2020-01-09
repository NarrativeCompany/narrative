<%--
  User: brian
  Date: Jan 29, 2008
  Time: 12:02:13 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.DatabaseConnectionStatusAction" />
<gct:wrapperCommonParams title="DB Connections"/>
<ge:clusterWrapper useServletSpecificBaseUrl="${true}">
    <gma:serverToolbar isDatabaseConnections="true"/>

    <table>
        <tr>
            <td>Partition</td>
            <td>Identity Token</td>
            <td>Num Idle Connections</td>
            <td>Num Busy Connections</td>
            <td>Total Connections in Pool</td>
        </tr>
        <g:forEach items="${action.partitionsMap}" obj="entry" className="java.util.Map.Entry">
            <g:set var="partitions" object="${entry.value}" className="java.util.Set" />
            <g:forEach items="${partitions}" obj="partition" className="org.narrative.network.core.cluster.partition.Partition">
                <g:set var="dataSource" object="${partition.dataSource}" className="org.narrative.common.persistence.hibernate.GDataSource" />
                <tr>
                    <td>${dataSource.dataSourceName}</td>
                    <td>${dataSource.identityToken}</td>
                    <td>${dataSource.numIdleConnectionsDefaultUser}</td>
                    <td>${dataSource.numBusyConnectionsDefaultUser}</td>
                    <td>${dataSource.numConnectionsDefaultUser}</td>
                </tr>
            </g:forEach>
        </g:forEach>
    </table>
</ge:clusterWrapper>