<%@ tag import="org.narrative.network.core.cluster.actions.CreateHQLQueryResultsAction" %>
<%@ tag import="org.narrative.network.core.cluster.actions.ManageUserKycStatusAction" %>
<%@ tag import="org.narrative.network.core.cluster.actions.TriggerTaskAction" %>
<%@ tag import="org.narrative.network.core.cluster.actions.server.SystemMonitoringAction" %>
<%@ tag import="org.narrative.network.core.master.admin.actions.ApplyFiatAdjustmentAction" %>
<%@ tag import="org.narrative.network.core.master.admin.actions.NeoTransactionsAction" %>
<%@ tag import="org.narrative.network.core.master.admin.actions.NeoWalletsAction" %>
<%@ tag import="org.narrative.network.core.master.admin.actions.rewards.RewardsRedemptionsAction" %>
<%@ tag import="org.narrative.network.core.cluster.actions.NarrativeSettingsAction" %>
<%@ tag import="org.narrative.network.core.cluster.actions.ManageCircleMembersAction" %>
<%@ tag import="org.narrative.network.customizations.narrative.niches.NarrativeCircleType" %>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>
<%@ taglib prefix="gtaa" tagdir="/WEB-INF/tags/legacy/area/admin" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<%-- bl: since the wrapper is used to generate error pages and error pages may need to be rendered
     for non-AreaActions, don't cast the action as an AreaAction. --%>
<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />

<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext"/>
<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />
<g:set var="area" object="${areaContext.area}" className="org.narrative.network.core.area.base.Area" />

<g:set var="subMenuResource" object="${action.subMenuResource}" className="java.lang.String" setNullIfNotOfType="true" />

<g:set var="nestedSubMenuResource" object="${action.nestedSubMenuResource}" className="java.lang.String" setNullIfNotOfType="true" />

${g:assert(networkContext.requestType.clusterCp, 'Only supporting legacy CP nav for the cluster cp now!')}

<g:set var="manageUserKycStatusActionName" object="<%=ManageUserKycStatusAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="manageUserKycStatusActionPath" object="<%=ManageUserKycStatusAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq manageUserKycStatusActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${manageUserKycStatusActionPath}"
        itemName="Manage User KYC"
/>

<g:set var="narrativeSettingsActionName" object="<%=NarrativeSettingsAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="narrativeSettingsActionPath" object="<%=NarrativeSettingsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq narrativeSettingsActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${narrativeSettingsActionPath}"
        itemName="Narrative Settings"
/>

<g:set var="neoWalletsActionName" object="<%=NeoWalletsAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="neoWalletsActionPath" object="<%=NeoWalletsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq neoWalletsActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${neoWalletsActionPath}"
        itemName="NEO Wallets"
/>

<g:set var="neoTransactionsActionName" object="<%=NeoTransactionsAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="neoTransactionsActionPath" object="<%=NeoTransactionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq neoTransactionsActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${neoTransactionsActionPath}"
        itemName="NEO Transactions"
/>

<g:set var="fiatAdjustmentActionName" object="<%=ApplyFiatAdjustmentAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="fiatAdjustmentActionPath" object="<%=ApplyFiatAdjustmentAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq fiatAdjustmentActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${fiatAdjustmentActionPath}"
        itemName="Apply Fiat Adjustment"
/>

<g:set var="rewardsRedemptionsActionName" object="<%=RewardsRedemptionsAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="rewardsRedemptionsActionPath" object="<%=RewardsRedemptionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        isOn="${subMenuResource eq rewardsRedemptionsActionName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}${rewardsRedemptionsActionPath}"
        itemName="Rewards Redemptions"
/>

<g:set var="circleMembersActionName" object="<%=ManageCircleMembersAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="circleMembersActionPath" object="<%=ManageCircleMembersAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<g:set var="circleMembersActionCircleParam" object="<%=ManageCircleMembersAction.CIRCLE_PARAM%>" className="java.lang.String" />
<g:forEach items="<%=NarrativeCircleType.MANAGEABLE_CIRCLE_TYPES%>" obj="circleType" className="org.narrative.network.customizations.narrative.niches.NarrativeCircleType">
    <g:url value="${action.networkRegistry.clusterCpRelativePath}${circleMembersActionPath}" id="circleUrl">
        <g:param name="${circleMembersActionCircleParam}" value="${circleType}" />
    </g:url>
    <gct:verticalMenuWrapperItem
            isOn="${subMenuResource eq circleMembersActionName and nestedSubMenuResource eq circleType}"
            itemLink="${circleUrl}"
            itemName="${circleType.name} Members"
    />
</g:forEach>

<g:set var="servletStatusSubMenuResource" object="<%=SystemMonitoringAction.SERVLET_STATUS_MENU_RESOURCE%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        id="current_servlet_menu"
        itemName="${action.networkRegistry.servletName}"
        itemLink="${action.networkRegistry.clusterCpRelativePath}/processes"
        isOn="${subMenuResource eq servletStatusSubMenuResource}" />

<g:set var="clusterAdminMenuResource" object="<%=CreateHQLQueryResultsAction.CLUSTER_ADMIN_MENU_RESOURCE%>" className="java.lang.String" />
<g:set var="hqlQueryActionName" object="<%=CreateHQLQueryResultsAction.ACTION_NAME%>" className="java.lang.String" />
<g:set var="triggerTaskActionName" object="<%=TriggerTaskAction.ACTION_NAME%>" className="java.lang.String" />
<gct:verticalMenuWrapperItem
        id="admin_menu"
        isLast="true"
        itemName="Admin"
        itemLink="${action.networkRegistry.clusterCpRelativePath}/${hqlQueryActionName}"
        isOn="${subMenuResource eq clusterAdminMenuResource}"
        isSubMenuOn="true">

    <gct:verticalMenuWrapperItem
        itemName="HQL Query"
        itemLink="${action.networkRegistry.clusterCpRelativePath}/${hqlQueryActionName}"
        isOn="${nestedSubMenuResource eq hqlQueryActionName}" />

    <gct:verticalMenuWrapperItem
        itemName="Trigger Tasks"
        itemLink="/${triggerTaskActionName}"
        isOn="${nestedSubMenuResource eq triggerTaskActionName}" />

</gct:verticalMenuWrapperItem>
