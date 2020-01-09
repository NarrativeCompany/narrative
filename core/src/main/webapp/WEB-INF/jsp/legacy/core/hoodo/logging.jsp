<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.server.LoggingInfoAction"/>

<gct:wrapperCommonParams title="Logging"/>
<ge:clusterWrapper useServletSpecificBaseUrl="${true}">
    <gma:serverToolbar isLoggingInfo="true"/>
    <table>
        <g:forEach items="${action.allLoggers}" varStatus="_stat" obj="logger" className="java.util.logging.Logger">
            <tr>
                <td>${_stat.index}</td>
                <td>${logger.name}</td>
                <td>${logger.level.name}</td>
            </tr>
        </g:forEach>
    </table>

</ge:clusterWrapper>