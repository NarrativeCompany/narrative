<%@ page import="org.narrative.network.core.master.admin.actions.rewards.ProcessRewardsRedemptionsAction" %>
<%@ page import="org.narrative.network.core.master.admin.actions.rewards.ProcessingRewardsRedemptionsJsonAction" %>
<%@ page import="org.narrative.network.core.master.admin.actions.rewards.MarkProcessingRewardsRedemptionsCompleteAction" %>
<%--
  User: brian
  Date: 2019-06-11
  Time: 09:13
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.rewards.RewardsRedemptionsAction" />

<ss:set var="processRedemptionsActionPath" object="<%=ProcessRewardsRedemptionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="processingRedemptionsJsonActionPath" object="<%=ProcessingRewardsRedemptionsJsonAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="markProcessingRedemptionsCompleteActionPath" object="<%=MarkProcessingRewardsRedemptionsCompleteAction.FULL_ACTION_PATH%>" className="java.lang.String" />

<gct:wrapperCommonParams title="Rewards Redemptions" />

<ge:clusterWrapper>
    <c:choose>
        <c:when test="${not empty action.processingRedemptions}">
            <h3>Redemptions In Process (${fn:length(action.processingRedemptions)})</h3>
            <gma:redemptionsTable transactions="${action.processingRedemptions}" transactionTotal="${action.processingRedemptionsTotal}" />

            <p>
                <a href="${processingRedemptionsJsonActionPath}/transfer-config.json">Export transfer-config.json</a>
            </p>

            <h3>Finalize Redemptions</h3>
            <gfs:form id="markProcessingRewardsRedemptionsCompleteFormId" action="${markProcessingRedemptionsCompleteActionPath}!execute" method="POST" showPleaseWaitOnSubmit="true" isMultipart="${true}">
                <gfs:file name="<%=MarkProcessingRewardsRedemptionsCompleteAction.JSON_FILE_DATA_PARAM%>" label="completed-bulk-transfer-jobs.json file" />

                <gfs:submit value="Mark All Redemptions In Process As Complete" />
            </gfs:form>
        </c:when>
        <c:when test="${empty action.pendingRedemptions}">
            There are no pending redemption requests to process.
        </c:when>
        <c:otherwise>
            <h4>Pending Redemption Requests (${fn:length(action.pendingRedemptions)})</h4>
            <gma:redemptionsTable transactions="${action.pendingRedemptions}" transactionTotal="${action.pendingRedemptionsTotal}" />
            <gfs:ajaxForm id="processRewardsRedemptionsFormId" action="${processRedemptionsActionPath}!execute" showPleaseWaitOnSubmit="true">
                <gfs:text
                        name="<%=ProcessRewardsRedemptionsAction.REDEMPTION_TEMP_WALLET_NEO_ADDRESS_PARAM%>"
                        value=""
                        size="60"
                        label="Redemption Temp Wallet NEO Address"
                />

                <gfs:submit value="Submit" />
            </gfs:ajaxForm>
        </c:otherwise>
    </c:choose>
</ge:clusterWrapper>