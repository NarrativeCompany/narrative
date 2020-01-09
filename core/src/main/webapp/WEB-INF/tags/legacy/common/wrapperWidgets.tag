<%@ tag pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<g:ref var="wrapperCommon_isPageInError" className="java.lang.Boolean" />
<c:if test="${not wrapperCommon_isPageInError and action.confirmationMessage!=null}">
    <gct:confirmationMessage
            confirmationMessageId="actionConfirmationMessageId"
            title="${action.confirmationMessage.title}"
            message="${action.confirmationMessage.message}"
            autoHideDelayMs="${action.confirmationMessage.autoHideDelayMS}"
    />
</c:if>

<div id="yuiTooltipContainer" class="yui-skin-sam"></div>
