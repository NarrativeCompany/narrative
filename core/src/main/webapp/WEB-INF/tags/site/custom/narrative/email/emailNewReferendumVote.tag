<%--
  User: jonmark
  Date: 4/3/18
  Time: 9:12 AM
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
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.referendum.services.SendReferendumVoteEmail" />
<ss:set var="vote" object="${task.vote}" className="org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote" />
<ss:set var="referendum" object="${vote.referendum}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" />
<ss:set var="channelConsumer" object="${referendum.channelConsumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />

<ss:set var="referendumTitle">
    <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" />
</ss:set>

<ss:set var="voterDisplayNameText">
    <s_page:displayNameText role="${vote.voter}" />
</ss:set>

<ss:set var="channelConsumerLink">
    <narrative:channelConsumerLink consumer="${channelConsumer}" expectedType="${referendum.type.channelType}"/>
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet2Arg('tags.site.custom.narrative.email.emailNewReferendumVote.subject', voterDisplayNameText, referendumTitle)}"
        actionText="${h:wordlet('tags.site.custom.narrative.email.emailNewReferendumVote.viewVotes')}"
        actionUrl="${referendum.displayVotesUrl}"
        watchedChannelConsumer="${channelConsumer}">
    <jsp:attribute name="details">
        <ss:set var="votedSuffix" object="${vote.votedFor ? '.for' : '.against'}" className="java.lang.String" />
        <ss:set var="typeSuffix" object=".${referendum.type}" className="java.lang.String" />

        <ss:set var="voterDisplayNameLink">
            <s_page:displayName role="${vote.voter}" />
        </ss:set>

        ${h:wordlet2Arg('tags.site.custom.narrative.email.emailNewReferendumVote.details'+=typeSuffix+=votedSuffix, voterDisplayNameLink, channelConsumerLink)}
    </jsp:attribute>

    <jsp:body>
        ${h:wordlet1Arg('tags.site.custom.narrative.email.emailNewReferendumVote.intro', channelConsumerLink)}
    </jsp:body>
</n_email:emailWrapper>
