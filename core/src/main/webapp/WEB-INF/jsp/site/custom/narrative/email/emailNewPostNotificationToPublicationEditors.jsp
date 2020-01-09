<%--
  User: brian
  Date: 3/16/19
  Time: 5:40 PM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.service.impl.publication.SendNewPostNotificationToPublicationEditorsEmailTask"/>

<ss:set var="content" object="${task.content}" className="org.narrative.network.core.content.base.Content" />
<ss:set var="publication" object="${task.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />

<ss:set var="displayUrl">
    <s_page:compositionConsumerUrl consumer="${content}" />
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.emailSubject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.reviewPost')}"
        actionUrl="${displayUrl}">
    <jsp:attribute name="details">
        <email:detailsTable>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.publication')}">
                <e:a href="${publication.displayUrl}">${publication.nameForHtml}</e:a>
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.author')}">
                <s_page:displayName role="${content.user}" showSmallAvatarForEmail="${true}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.posted')}">
                <comp:datetime datetime="${content.liveDatetime}" isNoPrettyTime="${true}" isLongFormat="${true}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.subject')}">
                <s_page:compositionConsumerLink consumer="${content}" linkSubject="${true}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.extract')}">
                ${content.extractForEmail}
            </email:detailsTableRow>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:body>
        <ss:set var="editorialReviewLink">
            <e:a href="${publication.manageReviewQueueUrl}">
                ${h:wordlet('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.editorialReview')}
            </e:a>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailNewPostNotificationToPublicationEditors.newPostRequiringReview', editorialReviewLink)}
    </jsp:body>
</n_email:emailWrapper>
