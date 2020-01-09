<%--
  User: jonmark
  Date: 3/19/18
  Time: 3:37 PM
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
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="ne_comp" tagdir="/WEB-INF/tags/site/custom/narrative/email/components" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.tribunal.services.SendNewTribunalIssueReportEmail"/>

<ss:set var="report" object="${task.report}" className="org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport" />
<ss:set var="channelConsumer" object="${report.tribunalIssue.referendum.channelConsumer}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />
<ss:set var="publication" object="${report.tribunalIssue.referendum.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />
<ss:set var="forEdit" object="${report.tribunalIssue.type.approveNicheDetailChange}" className="java.lang.Boolean" />

<ss:set var="statusSuffix" object="${forEdit ? '.forEdit' : task.forNewIssue ? '.forNewIssue' : ''}" className="java.lang.String" />
<ss:set var="typeSuffix" object="${report.tribunalIssue.type.ratifyPublication ? '.forRatifyPublication' : ''}" className="java.lang.String" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.subject'+=statusSuffix+=typeSuffix)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.viewIssue')}"
        actionUrl="${report.tribunalIssue.displayUrl}">
    <jsp:attribute name="details">
        <email:detailsTable>
            <c:choose>
                <c:when test="${channelConsumer.channelType.niche}">
                    <ne_comp:nicheDetailTableRow niche="${channelConsumer}" />
                </c:when>
                <c:otherwise>
                    ${ss:assert(channelConsumer.channelType.publication, 'Expected Publication ChannelConsumer, not/'+=channelConsumer.channelType)}

                    <ne_comp:publicationDetailTableRow publication="${channelConsumer}" />
                </c:otherwise>
            </c:choose>

            <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.reportedBy')}">
                <s_page:displayName role="${report.reporter.user}" showSmallAvatarForEmail="${true}" />
            </email:detailsTableRow>
            <c:choose>
                <c:when test="${forEdit}">
                    <ss:set var="metadata" object="${report.tribunalIssue.referendum.metadata}" className="org.narrative.network.customizations.narrative.niches.niche.NicheEditMetadataFieldsBase" />

                    <c:if test="${metadata.wasNameChanged}">
                        <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.originalName')}">
                            ${metadata.originalNameForHtml}
                        </email:detailsTableRow>
                        <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.newName')}">
                            ${metadata.newNameForHtml}
                        </email:detailsTableRow>
                    </c:if>
                    <c:if test="${metadata.wasDescriptionChanged}">
                        <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.originalDescription')}">
                            ${metadata.originalDescriptionForHtml}
                        </email:detailsTableRow>
                        <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.newDescription')}">
                            ${metadata.newDescriptionForHtml}
                        </email:detailsTableRow>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <email:detailsTableRow title="${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.report')}">
                        ${report.comments}
                    </email:detailsTableRow>
                </c:otherwise>
            </c:choose>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:body>
        ${h:wordlet('jsp.site.custom.narrative.email.emailNewTribunalIssueReport.intro'+=statusSuffix+=typeSuffix)}
    </jsp:body>
</n_email:emailWrapper>

