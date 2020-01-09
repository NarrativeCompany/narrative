<%@ page import="org.narrative.network.customizations.narrative.publications.Publication" %><%--
  User: jonmark
  Date: 2019-08-09
  Time: 14:04
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
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.publications.services.SendPublicationExpiringReminderEmail"/>

<ss:set var="publication" object="${task.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />
<ss:set var="wordletSuffix" object=".${task.reminderType}${task.publication.inTrialPeriod ? '.forTrial' : ''}" className="java.lang.String" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationExpiringReminder.subject'+=wordletSuffix)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationExpiringReminder.cta'+=wordletSuffix)}"
        actionUrl="${publication.displayUrl}">
    <e:elt name="p">
        <c:choose>
            <c:when test="${task.reminderType.expired}">
                <ss:set var="daysGracePeriod" object="<%=Publication.NON_ACTIVE_DURATION.toDays()%>" className="java.lang.Number" />

                ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPublicationExpiringReminder.intro'+=wordletSuffix, publication.nameForHtml, ss:formatNumber(daysGracePeriod))}
            </c:when>
            <c:otherwise>
                <ss:set var="endDatetime">
                    <comp:datetime datetime="${task.publication.endDatetimeTimestamp}" isNoPrettyTime="${true}" isLongFormat="${true}" />
                </ss:set>
                ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPublicationExpiringReminder.intro'+=wordletSuffix, publication.nameForHtml, endDatetime)}
            </c:otherwise>
        </c:choose>
    </e:elt>
</n_email:emailWrapper>
