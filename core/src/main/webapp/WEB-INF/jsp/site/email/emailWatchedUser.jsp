<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>


<ss:ref var="task" className="org.narrative.network.core.area.user.services.SendWatchedUserInstantNotificationTask" />
<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>

<%--to set the subject on the email --%>
<ss:set var="subject" scope="request" object="${task.notificationMessage}"/>

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.watchedUser.displayNameResolved}"
        title="${h:wordlet('jsp.site.email.emailWatchedUser.newFollower')}"
        actionUrl="${task.watchingUser.permalinkUrl}"
        actionText="${h:wordlet('jsp.site.email.emailWatchedUser.viewProfile')}"
        includeSignature="${true}">

    <jsp:attribute name="introduction">
        <ss:set var="updateYourSettingsLink">
            <e:a href="${task.areaContext.area.manageNotificationsUrl}">${h:wordlet('jsp.site.email.emailWatchedUser.updateYourSettings')}</e:a>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.email.emailWatchedUser.youAreReceivingThisBecause', updateYourSettingsLink)}
    </jsp:attribute>

    <jsp:attribute name="details">
        <email:userDetailsTable user="${task.watchingUser}" />
    </jsp:attribute>

</email:emailWrapper>
