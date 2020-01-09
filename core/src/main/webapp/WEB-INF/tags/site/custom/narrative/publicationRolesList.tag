<%--
  User: brian
  Date: 9/16/19
  Time: 2:38 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="roles" required="true" type="java.util.Collection" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<c:choose>
    <c:when test="${fn:length(roles)<=2}">
        <ss:forEach items="${roles}" obj="role" className="org.narrative.network.customizations.narrative.publications.PublicationRole" varStatus="roleStatus">
            <c:if test="${not roleStatus.first}">${' '}${h:wordlet('tags.site.custom.narrative.publicationRolesList.and')}${' '}</c:if>
            ${role.nameForDisplayWithArticle}
        </ss:forEach>
    </c:when>
    <c:otherwise>
        <ss:forEach items="${roles}" obj="role" className="org.narrative.network.customizations.narrative.publications.PublicationRole" varStatus="roleStatus">
            <c:if test="${not roleStatus.first}">, <c:if test="${roleStatus.last}">${h:wordlet('tags.site.custom.narrative.publicationRolesList.and')}${' '}</c:if></c:if>
            ${role.nameForDisplayWithArticle}
        </ss:forEach>
    </c:otherwise>
</c:choose>