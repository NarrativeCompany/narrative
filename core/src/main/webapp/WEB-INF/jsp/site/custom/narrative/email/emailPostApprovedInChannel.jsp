<%--
  User: jonmark
  Date: 2/21/18
  Time: 11:55 AM
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
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.posts.services.SendPostApprovedInChannelEmailTask"/>

<ss:set var="content" object="${task.content}" className="org.narrative.network.core.content.base.Content" />
<ss:set var="channelConsumer" object="${task.channel.consumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />
<ss:set var="moderatorUser" object="${task.moderatorUser}" className="org.narrative.network.core.user.User" />

<n_email:emailWrapper subject="${h:wordlet('jsp.site.custom.narrative.email.emailPostApprovedInChannel.subject')}">
    <ss:set var="contentUrl">
        <s_page:compositionConsumerUrl consumer="${content}" />
    </ss:set>
    <ss:set var="contentLink">
        <e:a href="${contentUrl}">
            ${content.subjectForHtml}
        </e:a>
    </ss:set>
    <ss:set var="channelLink">
        <e:a href="${channelConsumer.displayUrl}">
            ${channelConsumer.nameForHtml}
        </e:a>
    </ss:set>
    <ss:set var="moderatorLink">
        <e:a href="${moderatorUser.profileUrl}">
            ${moderatorUser.displayNameForHtml}
        </e:a>
    </ss:set>
    <e:elt name="p">
        ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailPostApprovedInChannel.introByModerator', contentLink, channelLink, moderatorLink)}
    </e:elt>
</n_email:emailWrapper>