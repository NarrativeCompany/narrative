<%--
  User: jonmark
  Date: 7/27/16
  Time: 3:07 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="reply" required="true" type="org.narrative.network.core.composition.base.Reply" %>
<%@ attribute name="consumer" required="true" type="org.narrative.network.core.composition.base.CompositionConsumer" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:set var="content" object="${consumer}" className="org.narrative.network.core.content.base.Content" setNullIfNotOfType="true" />
<ss:set var="referendum" object="${consumer}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" setNullIfNotOfType="true" />

<ss:set var="replyUrl"><s_page:compositionConsumerUrl consumer="${consumer}" reply="${reply}" /></ss:set>
<email:authorDetailsTable
        authorProvider="${reply}"
        byTitle="${h:wordlet('tags.site.email.displayReplyAuthorDetails.replyBy')}"
        compositionConsumer="${consumer}"
        portfolio="${consumer.portfolio}"
        subjectUrl="${replyUrl}"
        subjectText="${consumer.titleForDisplay}">
    <jsp:attribute name="subjectTitle">
        <ss:set var="subjectTitle">
            <c:choose>
                <c:when test="${ss:exists(referendum)}">
                    <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" />
                </c:when>
                <c:otherwise>
                    ${content.contentType.subjectFieldNameForDisplay}
                </c:otherwise>
            </c:choose>
        </ss:set>

        ${subjectTitle}
    </jsp:attribute>
</email:authorDetailsTable>
