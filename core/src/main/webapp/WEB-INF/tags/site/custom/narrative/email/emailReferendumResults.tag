<%--
  User: jonmark
  Date: 4/3/18
  Time: 10:06 AM
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
<%@ taglib prefix="ne_comp" tagdir="/WEB-INF/tags/site/custom/narrative/email/components" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.referendum.services.SendReferendumResultsEmail" />
<ss:set var="referendum" object="${task.referendum}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" />
<ss:set var="channelConsumer" object="${referendum.channelConsumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />

<ss:set var="referendumTitle">
    <narrative:referendumTitle referendum="${referendum}" dontLink="${true}" />
</ss:set>

<ss:set var="tribunalSuffix" object="${referendum.type.tribunalReferendum ? '.forTribunal' : ''}" className="java.lang.String" />
<ss:set var="publicationSuffix" object="${referendum.type.publicationRelated ? '.forPublication' : ''}" className="java.lang.String" />

<n_email:emailWrapper
        subject="${h:wordlet1Arg('tags.site.custom.narrative.email.emailReferendumResults.subject'+=tribunalSuffix, channelConsumer.nameForHtml)}"
        actionText="${h:wordlet('tags.site.custom.narrative.email.emailReferendumResults.viewReferendum'+=tribunalSuffix)}"
        actionUrl="${referendum.detailsUrl}"
        watchedChannelConsumer="${channelConsumer}">
    <jsp:attribute name="details">
        <email:detailsTable>
            <c:choose>
                <c:when test="${channelConsumer.channelType.niche}">
                    <ne_comp:nicheDetailTableRow niche="${channelConsumer}" />
                </c:when>
                <c:otherwise>
                    ${ss:assert(channelConsumer.channelType.publication, 'Expected Publication ChannelConsumer, but got/'+=channelConsumer.channelType)}
                    <ne_comp:publicationDetailTableRow publication="${channelConsumer}"/>
                </c:otherwise>
            </c:choose>
            <email:detailsTableRow title="${h:wordlet('tags.site.custom.narrative.email.emailReferendumResults.decision')}">
                <narrative:referendumResultText referendum="${referendum}" />
            </email:detailsTableRow>
        </email:detailsTable>
    </jsp:attribute>

    <jsp:body>
        ${h:wordlet('tags.site.custom.narrative.email.emailReferendumResults.intro'+=tribunalSuffix+=publicationSuffix)}
    </jsp:body>
</n_email:emailWrapper>
