<%--
  User: brian
  Date: 9/20/20
  Time: 3:36 PM
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
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.service.impl.publication.SendPublicationWriterAccountDeletedToPublicationEditorsEmailTask"/>

<ss:set var="publication" object="${task.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationWriterAccountDeletedToPublicationEditors.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationWriterAccountDeletedToPublicationEditors.viewPublication')}"
        actionUrl="${publication.displayUrl}">
    <ss:set var="postsText">
        <c:choose>
            <c:when test="${task.deletedPostCount==1}">
                ${h:wordlet('jsp.site.custom.narrative.email.emailPublicationWriterAccountDeletedToPublicationEditors.onePost')}
            </c:when>
            <c:otherwise>
                ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailPublicationWriterAccountDeletedToPublicationEditors.xPosts', h:formatNumber(task.deletedPostCount))}
            </c:otherwise>
        </c:choose>
    </ss:set>
    ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPublicationWriterAccountDeletedToPublicationEditors.writerDeletedAccount', task.userDisplayName, postsText)}
</n_email:emailWrapper>
