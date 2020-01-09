<%--
  User: jonmark
  Date: 11/6/12
  Time: 10:10 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="authorProvider" required="true" type="org.narrative.network.shared.services.AuthorProvider" %>
<%@ attribute name="subjectTitle" type="java.lang.String" %>
<%@ attribute name="subjectUrl" type="java.lang.String" %>
<%@ attribute name="subjectText" type="java.lang.String" %>
<%@ attribute name="byTitle" required="true" type="java.lang.String" %>
<%@ attribute name="portfolio" type="org.narrative.network.core.area.portfolio.Portfolio" %>
<%@ attribute name="compositionConsumer" type="org.narrative.network.core.composition.base.CompositionConsumer" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl"/>

<ss:set var="includeAuthorAvatar" object="${ss:exists(authorProvider.author)}" className="java.lang.Boolean" />

<email:detailsTable>
    <c:if test="${not empty subjectTitle and not empty subjectText}">
        <email:detailsTableRow title="${subjectTitle}">
            <c:choose>
                <c:when test="${not empty subjectUrl}">
                    <e:a href="${subjectUrl}">${subjectText}</e:a>
                </c:when>
                <c:otherwise>
                    ${subjectText}
                </c:otherwise>
            </c:choose>
        </email:detailsTableRow>
    </c:if>

    <email:detailsTableRow title="${byTitle}">
        <s_page:displayName role="${authorProvider.primaryRole}" showSmallAvatarForEmail="${includeAuthorAvatar}" />
    </email:detailsTableRow>

    <jsp:doBody />
</email:detailsTable>