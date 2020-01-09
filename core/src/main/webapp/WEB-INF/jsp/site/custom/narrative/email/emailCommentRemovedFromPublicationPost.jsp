<%--
  User: brian
  Date: 10/3/19
  Time: 9:29 AM
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.posts.services.SendCommentRemovedFromPublicationPostEmailTask"/>

<ss:set var="content" object="${task.content}" className="org.narrative.network.core.content.base.Content" />
<ss:set var="publication" object="${task.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />
<ss:set var="moderatorUser" object="${task.moderatorUser}" className="org.narrative.network.core.user.User" />

<n_email:emailWrapper subject="${h:wordlet('jsp.site.custom.narrative.email.emailCommentRemovedFromPublicationPost.subject')}">
    <jsp:attribute name="extraDetails">
        <email:detailsTable>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailCommentRemovedFromPublicationPost.post')}">
                <s_page:compositionConsumerLink consumer="${content}" linkSubject="${true}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailCommentRemovedFromPublicationPost.publication')}">
                <e:a href="${publication.displayUrl}">
                    ${publication.nameForHtml}
                </e:a>
            </email:detailsTableRow>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:attribute name="afterBody">
        ${h:wordlet('jsp.site.custom.narrative.email.emailCommentRemovedFromPublicationPost.reputationUnaffected')}
    </jsp:attribute>
    <jsp:body>
        <ss:set var="moderatorLink">
            <e:a href="${moderatorUser.profileUrl}">
                ${moderatorUser.displayNameForHtml}
            </e:a>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailCommentRemovedFromPublicationPost.intro', moderatorLink)}
    </jsp:body>
</n_email:emailWrapper>