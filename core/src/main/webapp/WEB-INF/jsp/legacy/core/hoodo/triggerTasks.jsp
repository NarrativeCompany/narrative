<%@ page import="org.narrative.network.core.system.Encryption" %>
<%--
  Created by IntelliJ IDEA.
  User: barry
  Date: Jan 10, 2007
  Time: 2:10:23 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<g:ref var="action" className="org.narrative.network.core.cluster.actions.TriggerTaskAction" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />

<g:set var="encryptionEnabled" object="<%=Encryption.INSTANCE.isAttachmentEncryptionEnabled()%>" className="java.lang.Boolean" />

<gct:wrapperCommonParams title="Trigger Tasks"/>
<ge:clusterWrapper>

    <h1>Current Servlet Tasks</h1>

    <gfs:form method="post" id="clearRedissonCacheFormId" name="clearRedissonCacheFormName" action="/triggerTask!clearRedissonCache">
        <gfs:text name="customString" value="" label="Cache Name" size="40" />
        <gfs:submit value="Clear Redisson Cache" />
    </gfs:form>

    <gfs:form method="post" id="clearAllRedissonCachesFormId" name="clearAllRedissonCachesFormName" action="/triggerTask!clearAllRedissonCaches">
        <gfs:submit value="Clear All Redisson Caches" />
    </gfs:form>

    <gfs:form method="post" id="triggerFlushHourStatsFormId" name="triggerFlushHourStatsFormName" action="/triggerTask!flushHourStats">
        <gfs:submit value="Flush Hourly Trending Stats on Servlet" />
    </gfs:form>

    <c:if test="${networkRegistry.localOrDevServer}">
        <gfs:form method="post" id="triggerCleanupOldFilesFormId" name="triggerCleanupOldFilesFormName" action="/triggerTask!runCleanupOldFiles">
            <gfs:submit value="Run Cleanup Old Files Task" />
        </gfs:form>
    </c:if>

    <h1>Global Task Queue Tasks</h1>

    <gfs:form method="post" id="calculateTrendingContentFormId" name="calculateTrendingContentFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!calculateTrendingContent">
        <gfs:submit value="Calculate Trending Content" />
    </gfs:form>

    <gfs:form method="post" id="removeStaleItemHourTrendingStatsTaskFormId" name="removeStaleItemHourTrendingStatsTaskFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!removeStaleItemHourTrendingStatsHandler">
        <gfs:submit value="Cleanup Expired Hourly Content Stats (older than 30 days)" />
    </gfs:form>

    <c:if test="${encryptionEnabled}">
        <gfs:form method="post" id="runEncryptFilesForClusterFormId" name="runEncryptFilesForClusterFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!runEncryptFilesForCluster">
            <c:if test="${networkRegistry.localOrDevServer}">
                <gfs:text name="areaOidOrDomain" value="" label="Area (OID or domain)" description="OPTIONAL" />
            </c:if>
            <gfs:submit value="Encrypt All Files For Cluster - ARE YOU SURE? THIS COULD TAKE A WHILE!" />
        </gfs:form>
        <gfs:form method="post" id="runDecryptFilesForClusterFormId" name="runDecryptFilesForClusterFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!runDecryptFilesForCluster">
            <c:if test="${networkRegistry.localOrDevServer}">
                <gfs:text name="areaOidOrDomain" value="" label="Area (OID or domain)" description="OPTIONAL" />
            </c:if>
            <gfs:submit value="Decrypt All Files For Cluster - ARE YOU SURE? THIS COULD TAKE A WHILE!" />
        </gfs:form>
    </c:if>

    <hr />
    <h2>Index Tasks</h2>

    <gfs:form method="post" id="triggerOptimizeIndexesFormId" name="optimizeIndexesFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!optimizeIndexes">
        <gfs:submit value="Optimize Indexes - ARE YOU SURE? THIS WILL TAKE A WHILE!" />
    </gfs:form>

    <gfs:form method="post" id="triggerAuditIndexMissingItemsFormId" name="auditIndexMissingItemsFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!auditIndexMissingItems">
        <gfs:checkboxlist name="indexTypes" value="${action.indexTypes}" list="${action.allIndexTypes}" label="Index Types" />
        <gfs:submit value="Audit Indexes for Missing Items" />
    </gfs:form>

    <gfs:form method="post" id="triggerRebuildIndexesFormId" name="rebuildIndexesFormName" action="${networkRegistry.clusterCpRelativePath}/triggerTask!rebuildIndexes">
        <gfs:checkboxlist name="indexTypes" value="${action.indexTypes}" list="${action.allIndexTypes}" label="Index Types" />
        <gfs:submit value="Rebuild Indexes - ARE YOU SURE? THIS WILL TAKE A WHILE!" />
    </gfs:form>
</ge:clusterWrapper>