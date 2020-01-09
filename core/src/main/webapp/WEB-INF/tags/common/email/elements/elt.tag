<%@ tag import="org.narrative.network.shared.email.*" %>
<%--
  User: jonmark
  Date: 10/31/12
  Time: 3:26 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ attribute name="cssClass" type="org.narrative.network.shared.email.EmailCssClass" %>
<%@ attribute name="cssClasses" type="java.util.Collection" %>
<%@ attribute name="cssClassNames" type="java.lang.String" %>

<%@ attribute name="href" type="java.lang.String" %>
<%@ attribute name="noFollow" type="java.lang.Boolean" %>
<%@ attribute name="targetBlank" type="java.lang.Boolean" %>
<%@ attribute name="dontResolveCss" type="java.lang.Boolean" %>

<%@ attribute name="src" type="java.lang.String" %>
<%@ attribute name="width" type="java.lang.Integer" %>
<%@ attribute name="height" type="java.lang.Integer" %>
<%@ attribute name="alt" type="java.lang.String" %>
<%@ attribute name="title" type="java.lang.String" %>
<%@ attribute name="rel" type="java.lang.String" %>
<%@ attribute name="colspan" type="java.lang.Integer" %>
<%@ attribute name="itemProp" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>

<ss:ref var="isJspEmail" className="java.lang.Boolean" />

<ss:set var="attributeMap" object="${ss:newMap()}" className="java.util.Map" />

<c:choose>
    <c:when test="${isJspEmail}">
        ${ss:assert(empty cssClassNames, 'Should never pre-define the cssClassNames for emails.  This value should always be derived from the EmailCssClass objects.')}

        <%--
            jw: it's not common, but there are times when we don't want to resolve the CSS because we are generating HTML to be used in a email, and the place it will be used will
                go through the HtmlEmailStyleParser, which will resolve the classes and element name CSS again, so this allows us to not double up on the CSS!
        --%>
        <c:if test="${not dontResolveCss}">
            <ss:set var="elementCss" object="<%=EmailElementCss.getCssForElementName(name)%>" className="java.lang.String" />

            <ss:set var="css">
                ${not empty elementCss ? elementCss : ''}
                ${cssClass ne null ? cssClass.css : ''}
                <ss:forEach items="${cssClasses}" obj="emailCssClass" className="org.narrative.network.shared.email.EmailCssClass">
                    ${emailCssClass ne null ? emailCssClass.css : ''}
                </ss:forEach>
            </ss:set>
            ${ss:mapPutNonEmpty(attributeMap, 'style', css)}
        </c:if>

        <ss:set var="cssClassNames">
            <c:if test="${cssClass ne null}">${cssClass.cssClass}</c:if><ss:forEach items="${cssClasses}" obj="emailCssClass" className="org.narrative.network.shared.email.EmailCssClass" varStatus="status"><c:if
                test="${cssClass ne null or not status.first}">${' '}</c:if>${emailCssClass.cssClass}</ss:forEach>
        </ss:set>
    </c:when>
    <c:otherwise>
        ${ss:assert(cssClass==null, 'Should not supply an EmailCssClass unless the JSP is only being generated for an email!')}
        ${ss:assert(fn:length(cssClasses)==0, 'Should not supply any EmailCssClasses unless the JSP is only being generated for an email!')}
    </c:otherwise>
</c:choose>

${ss:mapPutNonEmpty(attributeMap, 'class', cssClassNames)}

<c:if test="${not empty elementId}">
    ${ss:mapPut(attributeMap, 'id', elementId)}
</c:if>
<c:if test="${not empty href}">
    ${ss:mapPut(attributeMap, 'href', href)}
</c:if>
<%-- bl: don't need nofollow on JSP emails; those won't be indexed :) --%>
<c:if test="${noFollow and not isJspEmail}">
    ${ss:mapPut(attributeMap, 'rel', 'nofollow')}
</c:if>
${ss:mapPutNoNulls(attributeMap, 'target', targetBlank ? '_blank' : null)}
<c:if test="${not empty src}">
    ${ss:mapPut(attributeMap, 'src', src)}
</c:if>
<c:if test="${not empty width and width gt 0}">
    ${ss:mapPut(attributeMap, 'width', width)}
</c:if>
<c:if test="${not empty height and height gt 0}">
    ${ss:mapPut(attributeMap, 'height', height)}
</c:if>
<c:if test="${not empty alt}">
    ${ss:mapPut(attributeMap, 'alt', alt)}
</c:if>
<c:if test="${not empty title}">
    ${ss:mapPut(attributeMap, 'title', title)}
</c:if>
<c:if test="${not empty rel}">
    ${ss:mapPut(attributeMap, 'rel', title)}
</c:if>
<c:if test="${not empty colspan and colspan gt 1}">
    ${ss:assert(name eq 'td' or name eq 'th', 'Should only ever be using colspans on table data, or table header')}
    ${ss:mapPut(attributeMap, 'colspan', colspan)}
</c:if>
<c:if test="${not empty itemProp}">
    ${ss:mapPut(attributeMap, 'itemprop', itemProp)}
</c:if>

<ss:set var="isSelfClosed" object="<%=EmailElementCss.isElementSelfClosed(name)%>" className="java.lang.Boolean" />
<c:choose>
    <c:when test="${not isSelfClosed}">
        <${name}${ss:htmlAttributes(attributeMap)}><jsp:doBody /></${name}>
    </c:when>
    <c:otherwise>
        <${name}${ss:htmlAttributes(attributeMap)} />
    </c:otherwise>
</c:choose>
