<%@ tag import="org.narrative.network.core.cluster.partition.*" %>
<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="isProcesses" %>
<%@ attribute name="dbStatsPartitionType" %>
<%@ attribute name="isLoggingInfo" %>
<%@ attribute name="isAppliedPatches" %>
<%@ attribute name="isErrors" %>
<%@ attribute name="isDatabaseConnections" %>
<%@ attribute name="isThreadBucketStatus" %>
<%@ attribute name="isQuartzJobs" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.ClusterAction"/>

    <table style="height:30px;" class="c_align">
        <tr>
            <gct:headerLink tabName="Processes" isFirstTab="true"
                            tabLinkUrl="/processes" isActiveTab="${isProcesses}"/>
            <gct:headerLink tabName="Logging"
                            tabLinkUrl="/loggingInfo" isActiveTab="${isLoggingInfo}"/>
            <gct:headerLink tabName="Errors"
                            tabLinkUrl="/errors" isActiveTab="${isErrors}"/>
            <gct:headerLink tabName="DB Connections"
                            tabLinkUrl="/databaseConnections" isActiveTab="${isDatabaseConnections}"/>
            <gct:headerLink tabName="Thread Buckets"
                            tabLinkUrl="/threadBucketStatus" isActiveTab="${isThreadBucketStatus}"/>
            <gct:headerLink tabName="Quartz Jobs"
                            tabLinkUrl="/quartzJobs" isActiveTab="${isQuartzJobs}" isLastTab="true"/>
        </tr>
    </table>

<h1>Servlet: ${action.networkRegistry.servletName}</h1>
