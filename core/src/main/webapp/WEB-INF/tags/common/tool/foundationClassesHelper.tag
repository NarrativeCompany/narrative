<%--
  User: jonmark
  Date: 2/23/15
  Time: 4:12 PM

  The idea with this tag is to help centralize the process of creating a CSS class list for common foundation components.
  One of the great advantages of Foundation is that there are generic classes that are used on MANY of the display elements
  so we can use this tag to generate all relevant classes together.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="stringBuilder" type="java.lang.StringBuilder" %>

<%@ attribute name="isSecondary" type="java.lang.Boolean" %>
<%@ attribute name="isSuccess" type="java.lang.Boolean" %>
<%@ attribute name="isAlert" type="java.lang.Boolean" %>
<%@ attribute name="isWarning" type="java.lang.Boolean" %>
<%@ attribute name="isInfo" type="java.lang.Boolean" %>

<%@ attribute name="isSmall" type="java.lang.Boolean" %>
<%@ attribute name="isTiny" type="java.lang.Boolean" %>
<%@ attribute name="isRound" type="java.lang.Boolean" %>
<%@ attribute name="isRadius" type="java.lang.Boolean" %>

<%@ attribute name="isFullWidth" type="java.lang.Boolean" %>

<%@ attribute name="isRight" type="java.lang.Boolean" %>

<%@ attribute name="isDisabled" type="java.lang.Boolean" %>

<%@ attribute name="extraCssClasses" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>

<%--
    jw: output the style classes only if a cssClasses was not passed it.  If one was then lets assume the caller is handling
        using the cssClasses StringBuilder.
--%>
<ss:set var="outputClassesAtEnd" object="${stringBuilder eq null}" className="java.lang.Boolean" />
<c:if test="${stringBuilder eq null}">
    <ss:set var="stringBuilder" object="${ss:newStringBuilder()}" className="java.lang.StringBuilder" />
</c:if>

<c:choose>
    <c:when test="${isSecondary}">
        ${ss:addCssClass(stringBuilder, 'secondary')}
    </c:when>
    <c:when test="${isSuccess}">
        ${ss:addCssClass(stringBuilder, 'success')}
    </c:when>
    <c:when test="${isWarning}">
        ${ss:addCssClass(stringBuilder, 'warning')}
    </c:when>
    <c:when test="${isInfo}">
        ${ss:addCssClass(stringBuilder, 'info')}
    </c:when>
    <c:when test="${isAlert}">
        ${ss:addCssClass(stringBuilder, 'alert')}
    </c:when>
</c:choose>

<c:if test="${isSmall}">
    ${ss:addCssClass(stringBuilder, 'small')}
</c:if>

<c:if test="${isTiny}">
    ${ss:addCssClass(stringBuilder, 'tiny')}
</c:if>

<c:if test="${isRound}">
    ${ss:addCssClass(stringBuilder, 'round')}
</c:if>

<c:if test="${isRadius}">
    ${ss:addCssClass(stringBuilder, 'radius')}
</c:if>

<c:if test="${isFullWidth}">
    ${ss:addCssClass(stringBuilder, 'full-width')}
</c:if>

<c:if test="${isRight}">
    ${ss:addCssClass(stringBuilder, 'right')}
</c:if>

<c:if test="${isDisabled}">
    ${ss:addCssClass(stringBuilder, 'disabled')}
</c:if>

${ss:addCssClass(stringBuilder, extraCssClasses)}

<c:if test="${outputClassesAtEnd and not empty stringBuilder}">${stringBuilder}</c:if>