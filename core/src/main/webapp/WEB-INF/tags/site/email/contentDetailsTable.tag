<%@ tag import="org.narrative.network.core.fileondisk.image.ImageType" %>
<%--
  User: jonmark
  Date: 11/5/12
  Time: 8:47 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="content" required="true" type="org.narrative.network.core.content.base.Content" %>
<%@ attribute name="reply" type="org.narrative.network.core.composition.base.Reply" %>
<%@ attribute name="isNewContent" type="java.lang.Boolean" %>
<%@ attribute name="includePostDatetime" type="java.lang.Boolean" %>
<%@ attribute name="includeAssignee" type="java.lang.Boolean" %>
<%@ attribute name="assigneeOverride" type="org.narrative.network.core.area.user.AreaUser" %>
<%@ attribute name="showBestAnswer" type="java.lang.Boolean" %>
<%@ attribute name="dontLink" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="date" uri="http://www.narrative.org/tags/handy/date" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:set var="squareThumbnailImageType" object="<%=ImageType.SQUARE_THUMBNAIL%>" className="org.narrative.network.core.fileondisk.image.ImageType" />

<ss:set var="useReplyAuthor" object="${ss:exists(reply)}" className="java.lang.Boolean" />

<ss:set var="contentUrl"><c:choose>
    <c:when test="${ss:exists(reply)}">
        <s_page:compositionConsumerUrl consumer="${content}" reply="${reply}" />
    </c:when>
    <c:otherwise>${content.permalinkUrl}</c:otherwise>
</c:choose></ss:set>

<email:detailsTable>
    <email:detailsTableRow>
        <jsp:attribute name="title">
            ${content.contentType.subjectFieldNameForDisplay}:
        </jsp:attribute>
        <jsp:body>
            <c:choose>
                <c:when test="${dontLink}">
                    ${content.displaySubject}
                </c:when>
                <c:otherwise>
                    <e:a href="${contentUrl}">${content.displaySubject}</e:a>
                </c:otherwise>
            </c:choose>
        </jsp:body>
    </email:detailsTableRow>

    <c:if test="${includePostDatetime}">
        <email:detailsTableRow title="${h:wordlet('tags.email.common.contentDetailsTable.at')}">
            ${date:dateFormatLongDatetimeNoPrettyTime(content.liveDatetime)}
        </email:detailsTableRow>
    </c:if>

    <email:detailsTableRow title="${h:wordlet(useReplyAuthor ? 'tags.email.common.contentDetailsTable.replyBy' : 'tags.email.common.contentDetailsTable.by')}">
        <s_page:displayName role="${content.primaryRole}" showSmallAvatarForEmail="${true}" />
    </email:detailsTableRow>

    <jsp:doBody />
</email:detailsTable>