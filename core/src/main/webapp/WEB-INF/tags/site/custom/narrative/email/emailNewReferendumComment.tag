<%--
  User: jonmark
  Date: 4/2/18
  Time: 3:36 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.referendum.services.SendReferendumCommentInstantNotificationTask" />

<ss:set var="referendum" object="${task.referendum}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" />
<ss:set var="channelConsumer" object="${referendum.channelConsumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />

<ss:set var="referendumTitle">
    <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" />
</ss:set>

<ss:set var="compositionUrl">
    <s_page:compositionConsumerUrl consumer="${referendum}" reply="${task.reply}" />
</ss:set>

<ss:set var="authorDisplayName">
    <s_page:displayNameText role="${task.reply.primaryRole}" />
</ss:set>

<ss:set var="subject" scope="request" object="${h:wordlet3Arg('tags.site.custom.narrative.email.emailNewReferendumComment.subject', authorDisplayName, channelConsumer.nameForHtml, referendumTitle)}"/>

<email:emailWrapper
        subject="${subject}"
        useContextForSalutation="${true}"
        includeGenericSalutation="${true}"
        title="${h:wordlet1Arg('tags.site.custom.narrative.email.emailNewReferendumComment.newCommentToX', referendumTitle)}"
        actionUrl="${compositionUrl}"
        actionText="${h:wordlet('tags.site.custom.narrative.email.emailNewReferendumComment.viewThisComment')}"
        includeSignature="${false}"
        unsubscribeArea="${referendum.portfolio.area}">

    <jsp:attribute name="introduction">
        <ss:set var="channelConsumerLink">
            <narrative:channelConsumerLink consumer="${channelConsumer}" expectedType="${referendum.type.channelType}"/>
        </ss:set>
        ${h:wordlet2Arg('tags.site.custom.narrative.email.emailNewReferendumComment.introduction', referendumTitle, channelConsumerLink)}
    </jsp:attribute>

    <jsp:attribute name="details">
        <email:authorDetailsTable
                authorProvider="${task.reply}"
                subjectTitle="${h:wordlet('tags.site.custom.narrative.email.emailNewReferendumComment.'+=referendum.type.channelType)}"
                byTitle="${h:wordlet('tags.site.custom.narrative.email.emailNewReferendumComment.commentBy')}"
                compositionConsumer="${referendum}"
                portfolio="${referendum.portfolio}"
                subjectUrl="${compositionUrl}"
                subjectText="${ss:exists(channelConsumer) ? channelConsumer.nameForHtml : referendum.type.nameForDisplay}"
        />
    </jsp:attribute>

    <jsp:attribute name="information">
        <email:displayPostInEmail compositionConsumer="${referendum}" post="${task.reply}" />
    </jsp:attribute>

</email:emailWrapper>
