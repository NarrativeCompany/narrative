<%--
  User: jonmark
  Date: 4/22/13
  Time: 4:05 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="params" required="true" type="java.util.Map" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>

<ss:forEach items="${params}" obj="paramEntry" className="java.util.Map.Entry">
    <ss:set var="valueCollection" object="${paramEntry.value}" className="java.util.Collection" setNullIfNotOfType="true" />
    <c:choose>
        <c:when test="${not empty valueCollection}">
            <ss:forEach items="${valueCollection}" obj="value" className="java.lang.String">
                <ss:param name="${paramEntry.key}" value="${value}" />
            </ss:forEach>
        </c:when>
        <c:when test="${ss:isArray(paramEntry.value)}">
            <ss:forEach items="${paramEntry.value}" obj="value" className="java.lang.String">
                <ss:param name="${paramEntry.key}" value="${value}" />
            </ss:forEach>
        </c:when>
        <c:otherwise>
            <ss:param name="${paramEntry.key}" value="${paramEntry.value}" />
        </c:otherwise>
    </c:choose>
</ss:forEach>
