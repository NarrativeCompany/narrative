<%--
  User: jonmark
  Date: 7/27/16
  Time: 2:37 PM
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
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.mentions.SendNewMentionsInstantNotificationTask" />
<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>

<ss:set var="post" object="${task.post}" className="org.narrative.network.core.composition.base.PostBase" />
<ss:set var="content" object="${task.consumer}" className="org.narrative.network.core.content.base.Content" setNullIfNotOfType="true" />
<ss:set var="referendum" object="${task.consumer}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" setNullIfNotOfType="true" />
<ss:set var="reply" object="${task.reply}" className="org.narrative.network.core.composition.base.Reply" />

<ss:set var="forReply" object="${ss:exists(reply)}" className="java.lang.Boolean" />

<ss:set var="authorLink"><s_page:displayName role="${post.primaryRole}" /></ss:set>

<ss:set var="typeName">
    <c:choose>
        <c:when test="${forReply}">
            ${h:wordlet('content.comment')}
        </c:when>
        <c:when test="${ss:exists(referendum)}">
            <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" />
        </c:when>
        <c:otherwise>
            ${task.consumer.typeNameForDisplay}
        </c:otherwise>
    </c:choose>
</ss:set>
<ss:set var="typeNameLc">
    <c:choose>
        <c:when test="${ss:exists(referendum)}">
            <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" lowercase="${true}" />
        </c:when>
        <c:otherwise>
            ${task.consumer.typeLowercaseNameForDisplay}
        </c:otherwise>
    </c:choose>
    <c:if test="${forReply}">
        ${' '}${h:wordlet('content.comment.lowercase')}
    </c:if>
</ss:set>

<ss:set var="subject" object="${task.notificationMessage}" scope="request"/>

<email:emailWrapper
        subject="${subject}"
        useContextForSalutation="${true}"
        includeGenericSalutation="${true}"
        introduction="${h:wordlet2Arg('jsp.site.email.emailNewMention.youWereMentionedBy', authorLink, typeNameLc)}"
        actionUrl="${task.actionUrl}"
        actionText="${h:wordlet1Arg('jsp.site.email.emailNewMention.viewThisX', typeName)}"
        includeSignature="${true}"
        unsubscribeArea="${task.consumer.authZone.area}">

    <jsp:attribute name="details">
        <c:choose>
            <c:when test="${forReply}">
                <email:displayReplyAuthorDetails reply="${reply}" consumer="${task.consumer}" />
            </c:when>
            <c:when test="${ss:exists(content)}">
                <email:contentDetailsTable content="${content}" />
            </c:when>
            <c:otherwise>
                ${ss:throwUnexpectedError(ss:concat('The only way to get here is if we did not have a reply, and we did not have content, but those are the only two options. consumerType/', task.consumer.compositionConsumerType))}
            </c:otherwise>
        </c:choose>
    </jsp:attribute>

    <jsp:attribute name="information">
        <%-- jw: due to the nature of this email we will always have a post. --%>
        <email:displayPostInEmail compositionConsumer="${task.consumer}" post="${post}" />
    </jsp:attribute>

</email:emailWrapper>