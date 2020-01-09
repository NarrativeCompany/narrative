<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@attribute name="corners" required="true"%>
<%@attribute name="forceAllowCorners" type="java.lang.Boolean" %>
<%@attribute name="useTighterRounding" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<g:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<g:set var="isCssCornerBrowser" object="${true}"/>

<c:if test="${isCssCornerBrowser}">
    <g:set var="cornersToRound" object="${g:newMap()}" className="java.util.Map" />
    <c:choose>
        <c:when test="${fn:containsIgnoreCase(corners, 'none')}"/>
        <c:when test="${empty corners or fn:containsIgnoreCase(corners, 'all')}">
            ${g:mapPut(cornersToRound, 'tl', true)}
            ${g:mapPut(cornersToRound, 'tr', true)}
            ${g:mapPut(cornersToRound, 'bl', true)}
            ${g:mapPut(cornersToRound, 'br', true)}
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${fn:containsIgnoreCase(corners, 'top')}">
                    ${g:mapPut(cornersToRound, 'tl', true)}
                    ${g:mapPut(cornersToRound, 'tr', true)}
                </c:when>
                <c:otherwise>
                    <c:if test="${fn:containsIgnoreCase(corners, 'tl')}">
                        ${g:mapPut(cornersToRound, 'tl', true)}
                    </c:if>
                    <c:if test="${fn:containsIgnoreCase(corners, 'tr')}">
                        ${g:mapPut(cornersToRound, 'tr', true)}
                    </c:if>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${fn:containsIgnoreCase(corners, 'bottom')}">
                    ${g:mapPut(cornersToRound, 'bl', true)}
                    ${g:mapPut(cornersToRound, 'br', true)}
                </c:when>
                <c:otherwise>
                    <c:if test="${fn:containsIgnoreCase(corners, 'bl')}">
                        ${g:mapPut(cornersToRound, 'bl', true)}
                    </c:if>
                    <c:if test="${fn:containsIgnoreCase(corners, 'br')}">
                        ${g:mapPut(cornersToRound, 'br', true)}
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

    <g:forEach items="${cornersToRound}" obj="entry" className="java.util.Map.Entry"> do_rounded_div_css_ffsafari_${entry.key}</g:forEach>
    <c:if test="${useTighterRounding}"> do_tighter_rounding</c:if>
</c:if>
