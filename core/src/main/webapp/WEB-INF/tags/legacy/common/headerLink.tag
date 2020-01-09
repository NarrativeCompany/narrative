<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="tabName" required="true" type="java.lang.String" %>
<%@ attribute name="isActiveTab" type="java.lang.Boolean" %>
<%@ attribute name="isFirstTab" type="java.lang.Boolean" %>
<%@ attribute name="isLastTab" type="java.lang.Boolean" %>
<%@ attribute name="tabLinkUrl" required="true" type="java.lang.String" %>
<%@ attribute name="style" required="false" type="java.lang.String" %>

<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<c:choose>
    <c:when test="${isActiveTab}">
        ${tabName}
    </c:when>
    <c:otherwise>
        <a href="${tabLinkUrl}" style="${style}">${tabName}</a>
    </c:otherwise>
</c:choose>
${isLastTab? '' :' &middot; '}
