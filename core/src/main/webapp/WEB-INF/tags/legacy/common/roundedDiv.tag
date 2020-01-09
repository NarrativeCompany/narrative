<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="id"%>
<%@ attribute name="altCssClass" %>
<%@ attribute name="extraCssClass" %>
<%@ attribute name="style" %>
<%@ attribute name="innerStyle" %>
<%@ attribute name="title" %>
<%@attribute name="isAltBubble"%>
<%@attribute name="useNoBackgroundColor" type="java.lang.Boolean" %>
<%@attribute name="useBackgroundColor3" type="java.lang.Boolean" %>
<%@attribute name="isStatusBubble" type="java.lang.Boolean" %>
<%@attribute name="isPrivateContent" type="java.lang.Boolean" %>
<%@attribute name="corners" %>
<%@attribute name="forceAllowCorners" type="java.lang.Boolean" %>
<%@attribute name="disablePadding" %>
<%@attribute name="disableBottomMargin" type="java.lang.Boolean" %>
<%@attribute name="onClick" %>
<%@attribute name="highlightOnHover" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

${g:assert(not isPrivateContent or not highlightOnHover, 'privateContent does not currently support highlighting on hover!')}

<c:if test="${empty id}">
    <g:set var="id" object="RoundedDiv_${g:seq()}"/>
</c:if>

<g:set var="cssClass">
    <c:choose>
        <c:when test="${isAltBubble}">info_bar_section_alt backgroundColor2</c:when>
        <c:when test="${useNoBackgroundColor}">inheritStyling</c:when>
        <c:when test="${useBackgroundColor3}">backgroundColor3</c:when>
        <c:when test="${isStatusBubble}">statusBackgroundColor</c:when>
        <c:when test="${isPrivateContent}">privateContent</c:when>
        <c:when test="${not empty altCssClass}">${altCssClass}</c:when>
        <c:otherwise>info_bar_section backgroundColor1</c:otherwise>
    </c:choose>
    <gct:roundedDivClasses corners="${corners}" forceAllowCorners="${forceAllowCorners}" />
    <c:if test="${not empty extraCssClass}">${' '}${extraCssClass}</c:if>
    <c:if test="${empty altCssClass}">${' '}shadedContentArea</c:if>
    <c:choose>
        <c:when test="${not highlightOnHover}" />
        <c:when test="${useNoBackgroundColor}"> backgroundColor1OnHover</c:when>
        <c:otherwise> backgroundColor2OnHover</c:otherwise>
    </c:choose>
    <c:if test="${highlightOnHover}"> backgroundColor2OnHover</c:if>
    <c:if test="${disableBottomMargin}"> disableBottomMargin</c:if>
</g:set>

<g:set var="paddingDivId" object="${id}InnerDiv" />

<g:set var="roundedDivHtml">
    <div id="${id}"
        ${g:condAttr('class',cssClass)}
        ${g:condAttr('style', style)}
        ${g:condAttr('title', title)}
        ${g:condAttr('onclick', onClick)}>
        <c:choose>
            <c:when test="${disablePadding}">
                <jsp:doBody />
            </c:when>
            <c:otherwise>
                <div class="bubble_padding" ${g:condAttr('id',paddingDivId)} ${g:condAttr('style', innerStyle)}>
                   <jsp:doBody />
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</g:set>

${roundedDivHtml}
