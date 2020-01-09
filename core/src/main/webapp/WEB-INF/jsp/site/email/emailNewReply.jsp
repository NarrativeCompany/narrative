<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.watchlist.services.SendReplyInstantNotificationTask" />
<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>

<ss:set var="replyUrl"><s_page:compositionConsumerUrl consumer="${task.content}" reply="${task.reply}" /></ss:set>

<ss:set var="authorDisplayName">
    <s_page:displayNameText role="${task.reply.primaryRole}" />
</ss:set>
<ss:set var="subject" scope="request" object="${task.notificationMessage}"/>

<email:emailWrapper
        subject="${subject}"
        useContextForSalutation="${true}"
        title="${h:wordlet1Arg('jsp.site.email.emailNewReply.newReplyToContentType',
                                task.content.contentType.nameForDisplay)}"
        actionUrl="${replyUrl}"
        actionText="${h:wordlet('jsp.site.email.emailNewReply.viewThisReply')}"
        includeGenericSalutation="${true}"
        includeSignature="${true}"
        unsubscribeArea="${task.compositionConsumer.portfolio.area}"
        unsubscribeFromGlobal="${true}">

    <jsp:attribute name="introduction">
        ${h:wordlet('jsp.site.email.emailNewReply.wereSendingYouThisNotificationBecause')}
    </jsp:attribute>

    <jsp:attribute name="details">
        <email:authorDetailsTable
                authorProvider="${task.reply}"
                subjectTitle="${h:wordlet('jsp.site.email.emailNewReply.subject')}"
                byTitle="${h:wordlet('jsp.site.email.emailNewReply.replyBy')}"
                compositionConsumer="${task.content}"
                portfolio="${task.content.portfolio}"
                subjectUrl="${replyUrl}"
                subjectText="${task.content.subject}"
        />
    </jsp:attribute>

    <jsp:attribute name="information">
        <email:displayPostInEmail compositionConsumer="${task.content}" post="${task.reply}" />
    </jsp:attribute>

</email:emailWrapper>
