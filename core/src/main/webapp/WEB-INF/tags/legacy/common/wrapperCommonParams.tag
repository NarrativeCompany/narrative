<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="isPageInError" type="java.lang.Boolean" description="Indicates if this is an error page.  Used for exception pages, file not found pages, and for cookies not enabled error page.  Used to suppress output of RSS links and confirmation messages.  Defaults to false." %>
<%@ attribute name="title" type="java.lang.String" description="used in basicWrapper for the html title" %>
<%@ attribute name="extraHeadTag" type="java.lang.String" description="" %>
<%@ attribute name="customAreaHeaderColumn" type="java.lang.String" description="used to display title other than default in areaWrapper" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>

<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext"/>
<g:ref var="networkAction" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<g:set var="areaContext" object="${gn:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />


<c:if test="${isPageInError}">
    <g:set var="wrapperCommon_isPageInError" scope="request"  object="${isPageInError}" className="java.lang.Boolean"/>
    <g:set var="wrapperCommon_isPageInErrorWithNoSession" scope="request"  object="${not networkContext.globalSessionAvailable}" className="java.lang.Boolean"/>
</c:if>

<c:if test="${not empty title}">
    <g:set var="wrapperCommon_title" scope="request" className="java.lang.String">
        <c:if test="${isPageInError}">Error: </c:if>
        ${title}
    </g:set>
    <g:set var="wrapperCommon_bodyTitle" scope="request" className="java.lang.String">
        ${isPageInError ? 'Error: ' : ''}
        ${title}
    </g:set>
</c:if>

<c:if test="${not empty customAreaHeaderColumn}">
    <g:set var="wrapperCommon_customAreaHeaderColumn" scope="request" object="${customAreaHeaderColumn}" className="java.lang.String"/>
</c:if>

<c:if test="${not empty extraHeadTag}">
    <g:set var="wrapperCommon_extraHeadTag" scope="request" className="java.lang.String">
        ${wrapperCommon_extraHeadTag}
        ${extraHeadTag}
    </g:set>
</c:if>
