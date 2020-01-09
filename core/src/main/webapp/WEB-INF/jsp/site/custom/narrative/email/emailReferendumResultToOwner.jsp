<%--
  User: jonmark
  Date: 3/6/18
  Time: 2:04 PM

  The hardest part with this email is making sure we get all the combinations of referendum type to result type, and that
  is because of the fact that we only have to worry about combinations where the Niche could have had an owner at the beginning
  of the process.  As a result of that, the only times when a niche should ever have a owner is during for the following
  referendum types:
  * TRIBUINAL_APPROVE_NICHE_DETAIL_CHANGE
  * RATIFY_NICHE
  * TRIBUNAL_RATIFY_NICHE
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
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="ne_comp" tagdir="/WEB-INF/tags/site/custom/narrative/email/components" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.referendum.services.SendReferendumResultToOwnerEmail"/>

<ss:set var="referendum" object="${task.referendum}" className="org.narrative.network.customizations.narrative.niches.referendum.Referendum" />
<ss:set var="channelConsumer" object="${referendum.channelConsumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />

${ss:assert(not referendum.type.approveSuggestedNiche , 'Suggested niches should not have an owner yet, so this email should never be sent!')}

<ss:set var="channelConsumerLink">
    <narrative:channelConsumerLink consumer="${channelConsumer}" expectedType="${referendum.type.channelType}"/>
</ss:set>

<ss:set var="messageText">
    <narrative:referendumResultText referendum="${referendum}"/>
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.subject.'+=referendum.type)}"
        actionText="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailReferendumResultToOwner.viewChannelConsumer', channelConsumer.nameForHtml)}"
        actionUrl="${channelConsumer.displayUrl}">
    <jsp:attribute name="details">
        <email:detailsTable>
            <c:choose>
                <c:when test="${channelConsumer.channelType.niche}">
                    <ne_comp:nicheDetailTableRow niche="${channelConsumer}"/>
                </c:when>
                <c:otherwise>
                    ${ss:assert(channelConsumer.channelType.publication, 'Expected Publication but got/'+=channelConsumer.channelType)}

                    <ne_comp:publicationDetailTableRow publication="${channelConsumer}"/>
                </c:otherwise>
            </c:choose>
            <c:if test="${referendum.type.tribunalApproveNicheDetails}">
                <ss:set var="metadata" object="${referendum.metadata}" className="org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata" />
                <c:if test="${metadata.wasNameChanged}">
                    <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.requestedName')}">
                        ${metadata.newNameForHtml}
                    </email:detailsTableRow>
                </c:if>
                <c:if test="${metadata.wasDescriptionChanged}">
                    <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.requestedDescription')}">
                        ${metadata.newDescriptionForHtml}
                    </email:detailsTableRow>
                </c:if>
            </c:if>
            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.results')}">
                <ss:set var="votesFor">
                    <c:choose>
                        <c:when test="${referendum.tribunalVotesFor==1}">
                            ${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.oneVote')}
                        </c:when>
                        <c:otherwise>
                            ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailReferendumResultToOwner.votesCount', referendum.tribunalVotesFor)}
                        </c:otherwise>
                    </c:choose>
                </ss:set>
                <ss:set var="votesAgainst">
                    <c:choose>
                        <c:when test="${referendum.tribunalVotesAgainst==1}">
                            ${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.oneVote')}
                        </c:when>
                        <c:otherwise>
                            ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailReferendumResultToOwner.votesCount', referendum.tribunalVotesAgainst)}
                        </c:otherwise>
                    </c:choose>
                </ss:set>
                ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailReferendumResultToOwner.resultsValue', referendum.approvalPercentage.formattedAsTwoDecimalPercentage, votesFor, votesAgainst)}
            </email:detailsTableRow>
            <%-- jw: note: due to email preview, we are only going to be checking for null state  --%>
            <c:if test="${referendum.tribunalIssue ne null}">
                <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailReferendumResultToOwner.relatedIssue')}">
                    ${channelConsumerLink}
                </email:detailsTableRow>
            </c:if>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:body>
        <e:elt name="p">
            ${messageText}
        </e:elt>
    </jsp:body>
</n_email:emailWrapper>
