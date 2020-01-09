<%@ page import="org.narrative.network.core.cluster.actions.NarrativeSettingsAction" %><%--
  User: jonmark
  Date: 2020-01-03
  Time: 10:39
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

<ss:ref var="action" className="org.narrative.network.core.cluster.actions.NarrativeSettingsAction" />

<gct:wrapperCommonParams title="Narrative Settings" />

<ge:clusterWrapper>
    <ss:set var="actionPath" object="<%=NarrativeSettingsAction.FULL_ACTION_PATH%>" className="java.lang.String" />

    <gfs:form id="narrativeSettingsForm" action="${actionPath}!execute" method="POST" showPleaseWaitOnSubmit="true">
        <gfs:text
                name="<%=NarrativeSettingsAction.NRVE_SCRIPT_HASH_PARAM%>"
                value="${action.nrveScriptHash}"
                label="NRVE Script Hash"
                required="${true}"
                size="60"
        />

        <gfs:checkbox
                name="<%=NarrativeSettingsAction.FIAT_PAYMENTS_ENABLED_PARAM%>"
                value="${action.fiatPaymentsEnabled}"
                label="Enable Credit Card Payments"
                description="When enabled, members will be able to purchase Niches through PayPal."
        />

        <gfs:text
                name="<%=NarrativeSettingsAction.SHUTDOWN_NOTICE_URL_PARAM%>"
                value="${action.shutdownNoticeUrl}"
                required="${false}"
                label="Shutdown Notice URL"
                description="When specified, a shutdown notice will appear at the top of the page when the app first loads."
                size="80"
        />
        <gfs:submit value="Update Settings" />
    </gfs:form>
</ge:clusterWrapper>