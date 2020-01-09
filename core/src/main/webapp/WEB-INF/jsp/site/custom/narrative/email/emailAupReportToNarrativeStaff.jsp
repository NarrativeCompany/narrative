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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.service.impl.tribunal.SendAupReportToNarrativeStaffEmailTask"/>

<ss:set var="ratable" object="${task.ratable}" className="org.narrative.network.core.rating.Ratable" />
<ss:set var="content" object="${ratable.ratableType.content ? ratable : null}" className="org.narrative.network.core.content.base.Content" />
<ss:set var="reply" object="${ratable.ratableType.reply ? ratable : null}" className="org.narrative.network.core.composition.base.Reply" />

<ss:set var="compositionConsumer" object="${ratable.ratableType.content ? content : reply.composition.compositionConsumer}" className="org.narrative.network.core.composition.base.CompositionConsumer" />

<ss:set var="displayUrl">
    <s_page:compositionConsumerUrl consumer="${compositionConsumer}" reply="${reply}" />
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.subject.'+=ratable.ratableType)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.view.'+=ratable.ratableType)}"
        actionUrl="${displayUrl}">
    <jsp:attribute name="extraDetails">
        <email:detailsTable>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.reportedBy')}">
                <s_page:displayName role="${task.reporter}" showSmallAvatarForEmail="${true}" />
            </email:detailsTableRow>
            <c:if test="${not empty task.reason}">
                <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.reason')}">
                    ${task.reason}
                </email:detailsTableRow>
            </c:if>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:attribute name="details">
        <email:detailsTable>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.author')}">
                <s_page:displayName role="${ratable.user}" showSmallAvatarForEmail="${true}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.posted')}">
                <comp:datetime datetime="${ratable.liveDatetime}" isNoPrettyTime="${true}" isLongFormat="${true}" />
            </email:detailsTableRow>
            <c:if test="${ratable.ratableType.content}">
                <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.subject')}">
                    <s_page:compositionConsumerLink consumer="${content}" linkSubject="${true}" />
                </email:detailsTableRow>
            </c:if>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailAupReportToNarrativeStaff.extract')}">
                ${ratable.extractForEmail}
            </email:detailsTableRow>
        </email:detailsTable>
    </jsp:attribute>
</n_email:emailWrapper>
