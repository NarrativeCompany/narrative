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
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.posts.services.SendPostRemovedFromChannelEmailTask"/>

<ss:set var="content" object="${task.content}" className="org.narrative.network.core.content.base.Content" />
<ss:set var="channelConsumer" object="${task.channel.consumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />
<ss:set var="moderatorUser" object="${task.moderatorUser}" className="org.narrative.network.core.user.User" />
<ss:set var="baseWordletSuffix" object=".${channelConsumer.channelType}" className="java.lang.String" />
<ss:set var="wordletSuffix" object="${task.forModeration ? '.forModeration' : task.forChannelDeletion ? '.forChannelDeletion' : null}${baseWordletSuffix}" className="java.lang.String" />

<n_email:emailWrapper subject="${h:wordlet('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.subject'+=wordletSuffix)}">
    <jsp:attribute name="details">
        <c:if test="${not empty task.message}">
            <email:detailsTable>
                <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.actor' += baseWordletSuffix)}">
                    <s_page:displayName role="${task.moderatorUser}" showSmallAvatarForEmail="${true}" />
                </email:detailsTableRow>
                <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.message')}">
                    ${task.message}
                </email:detailsTableRow>
            </email:detailsTable>
        </c:if>
    </jsp:attribute>
    <jsp:body>
        <ss:set var="contentUrl">
            <s_page:compositionConsumerUrl consumer="${content}" />
        </ss:set>
        <ss:set var="contentLink">
            <%-- bl: use the edit URL if the post was moderated and has now been returned to drafts --%>
            <e:a href="${task.forModeration ? content.editContentUrl : contentUrl}">
                ${content.subjectForHtml}
            </e:a>
        </ss:set>
        <ss:set var="channelLink">
            <e:a href="${channelConsumer.displayUrl}">
                ${channelConsumer.nameForHtml}
            </e:a>
        </ss:set>
        <e:elt name="p">
            <c:choose>
                <c:when test="${ss:exists(moderatorUser)}">
                    <ss:set var="moderatorLink">
                        <e:a href="${moderatorUser.profileUrl}">
                            ${moderatorUser.displayNameForHtml}
                        </e:a>
                    </ss:set>
                    ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.introByModerator'+=wordletSuffix, contentLink, channelLink, moderatorLink)}
                    <c:if test="${task.forModeration and not content.live}">
                        ${' '}
                        ${h:wordlet('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.postReturnedToDrafts')}
                    </c:if>
                </c:when>
                <c:when test="${task.forChannelDeletion}">
                    <%--
                        jw: if there is no moderator user, then let's handle the case where the channel was deleted. We should
                            never have a case where we have a moderator user and we are sending the email for a publication deletion.
                    --%>
                    ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.introForDeletedChannel'+=wordletSuffix, contentLink, channelConsumer.nameForHtml)}
                </c:when>
                <c:when test="${task.channel.type.publication}">
                    <%--
                        jw: We cannot include a link to the publication since it is being deleted as part of rejection. Let's use
                            the same message, but just use the publication name without the link.
                    --%>
                    ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.introForRejectedChannel'+=wordletSuffix, contentLink, task.channel.consumer.nameForHtml)}
                </c:when>
                <c:otherwise>
                    <%-- bl: if there is no moderator user, and it's not for channel deletion then it means the post was
                         removed because the Channel was rejected --%>
                    ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.introForRejectedChannel'+=wordletSuffix, contentLink, channelLink)}
                </c:otherwise>
            </c:choose>
        </e:elt>
        <c:if test="${not content.publishedToPersonalJournal and empty content.publishedToChannels and not task.forModeration}">
            <ss:set var="editContentLink">
                <e:a href="${content.editContentUrl}">
                    ${h:wordlet('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.editYourPost')}
                </e:a>
            </ss:set>
            <e:elt name="p">
                ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailPostRemovedFromChannel.orphanedPost' += (channelConsumer.channelType.publication ? '.forPublication' : null), editContentLink)}
            </e:elt>
        </c:if>
    </jsp:body>
</n_email:emailWrapper>