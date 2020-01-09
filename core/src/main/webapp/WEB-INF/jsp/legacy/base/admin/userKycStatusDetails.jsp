<%@ page import="org.narrative.network.core.cluster.actions.UpdateUserKycStatusAction" %>
<%@ page import="org.narrative.network.core.cluster.actions.UpdateUserKycMetadataAction" %>
<%@ page import="org.narrative.network.core.locations.Country" %>
<%@ page import="org.narrative.network.core.user.KycImageType" %>
<%@ page import="org.narrative.network.core.cluster.actions.SendUserKycImageAction" %>
<%@ page import="org.narrative.network.core.cluster.actions.RejectUserKycDocumentAction" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="date" uri="http://www.narrative.org/tags/handy/date" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="dom" tagdir="/WEB-INF/tags/common/dom" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/legacy/common" %>

<ss:ref var="action" className="org.narrative.network.core.cluster.actions.UserKycStatusDetailsAction"/>

<ss:set var="userKyc" object="${action.userKyc}" className="org.narrative.network.core.user.UserKyc" />
<ss:set var="user" object="${userKyc.user}" className="org.narrative.network.core.user.User" />

<style>
    .mediumWidth {
        width: 300px;
    }
</style>

<div class="pageContentTopDescription">
    <table width="100%" style="margin-left: 20px; margin-right: 20px;">
        <th style="margin-bottom: 20px">
            <h3>User KYC Details</h3>
        </th>
        <tr>
            <td class="mediumWidth">
                <strong>Handle</strong>
            </td>
            <td>${userKyc.user.username}</td>
        </tr>
        <tr>
            <td class="mediumWidth">
                <strong>Last Updated</strong>
            </td>
            <td>${action.formatInstant(userKyc.lastUpdated)}</td>
        </tr>
        <tr>
            <td class="mediumWidth">
                <strong>KYC Status</strong>
            </td>
            <td>
                <c:choose>
                    <c:when test="${userKyc.kycStatus.rejected}">
                        <span style="color: red;">${userKyc.kycStatus.nameForDisplay}</span>
                    </c:when>
                    <c:otherwise>
                        ${userKyc.kycStatus.nameForDisplay}
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <c:if test="${userKyc.country != null}">
            <tr>
                <td class="mediumWidth">
                    <strong>Country</strong>
                </td>
                <td>${userKyc.country.nameForDisplay}</td>
            </tr>
        </c:if>
        <c:if test="${userKyc.birthMonth != null and userKyc.birthYear != null}">
            <tr>
                <td class="mediumWidth">
                    <strong>Month of Birth</strong>
                </td>
                <td>${userKyc.birthMonth} / ${userKyc.birthYear}</td>
            </tr>
        </c:if>
        <c:if test="${userKyc.submissionCount > 0}">
            <ss:set var="sendKycImageActionPath" object="<%=SendUserKycImageAction.FULL_ACTION_PATH%>" className="java.lang.String" />
            <tr>
                <td class="mediumWidth">
                    <strong>KYC Files</strong>
                </td>
                <td>
                    <ul>
                        <ss:forLoop begin="1" end="${userKyc.submissionCount}" varStatus="loopStatus">
                            <ss:forEach items="<%=KycImageType.values()%>" obj="imageType" className="org.narrative.network.core.user.KycImageType">
                                <li>
                                    <a href="${action.networkRegistry.clusterCpRelativePath}${sendKycImageActionPath}/<%=SendUserKycImageAction.Fields.userKyc%>/${userKyc.oid}/<%=SendUserKycImageAction.Fields.imageType%>/${imageType}/<%=SendUserKycImageAction.Fields.submission%>/${loopStatus.index}" target="_blank">
                                        ${h:wordlet(imageType.fieldNameWordletKey)}
                                        <c:if test="${loopStatus.index > 1}">
                                            (${loopStatus.index})
                                        </c:if>
                                    </a>
                                </li>
                            </ss:forEach>
                        </ss:forLoop>
                    </ul>
                </td>
            </tr>
        </c:if>
    </table>

    <br />
    <br />

    <ss:set var="updateKycStatusActionPath" object="<%=UpdateUserKycStatusAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <ss:set var="updateKycMetadataActionPath" object="<%=UpdateUserKycMetadataAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <ss:set var="rejectKycDocumentActionPath" object="<%=RejectUserKycDocumentAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <ss:set var="countries" object="<%=Country.getOrderedCountries()%>" className="java.util.Set" />

    <c:choose>
        <c:when test="${userKyc.kycStatus.awaitingMetadata}">
            <div class="pageContentTopDescription">
                <h2>Enter Document Metadata</h2>
                <gfs:ajaxForm id="updateKycMetadataForm" action="${action.networkRegistry.clusterCpRelativePath}${updateKycMetadataActionPath}!execute" showPleaseWaitOnSubmit="${true}" reloadPageOnSuccess="${true}">
                    <gfs:hidden name="userKyc" value="${userKyc.oid}" />

                    <div class="pageContentTopDescription">
                        <gfs:text
                                name="<%=UpdateUserKycMetadataAction.Fields.firstName%>"
                                value=""
                                label="First Name"
                        />
                        <gfs:text
                                name="<%=UpdateUserKycMetadataAction.Fields.lastName%>"
                                value=""
                                label="Last Name"
                        />
                        <gfs:select
                                name="<%=UpdateUserKycMetadataAction.Fields.country%>"
                                value=""
                                list="${countries}"
                                listValue="nameForDisplay"
                                label="Country"
                                extraKey=""
                                extraValue="Choose One"
                                addExtraItemToTop="${true}"
                        />
                        <gfs:text
                                name="<%=UpdateUserKycMetadataAction.Fields.documentNumber%>"
                                value=""
                                label="Document Number"
                        />
                        <gfs:birthdaySelector
                                name="<%=UpdateUserKycMetadataAction.Fields.dateOfBirth%>"
                                value="${null}"
                                label="Date of Birth"
                        />
                        <gfs:submit
                                value="Update User KYC Metadata"
                        />
                    </div>
                </gfs:ajaxForm>

                <h2>Metadata Missing?</h2>
                <gfs:ajaxForm id="rejectKycDocumentForm" action="${action.networkRegistry.clusterCpRelativePath}${rejectKycDocumentActionPath}!execute" showPleaseWaitOnSubmit="${true}" reloadPageOnSuccess="${true}">
                    <gfs:hidden name="userKyc" value="${userKyc.oid}" />
                    <gfs:text
                            name="<%=RejectUserKycDocumentAction.Fields.note%>"
                            value=""
                            label="Note"
                    />
                    <gfs:submit
                            value="Reject Document for Missing Metadata"
                    />
                </gfs:ajaxForm>
            </div>

            <br />
        </c:when>
        <c:when test="${userKyc.kycStatus.eligibleForManualChange}">
            <gfs:ajaxForm id="updateKycStatusForm" action="${action.networkRegistry.clusterCpRelativePath}${updateKycStatusActionPath}!execute" showPleaseWaitOnSubmit="${true}" reloadPageOnSuccess="${true}">
                <gfs:hidden name="userKyc" value="${userKyc.oid}" />

                <div class="pageContentTopDescription">
                    <table width="100%" style="margin-left: 20px; margin-right: 20px;">
                        <tr>
                            <th><h3>Update User KYC Status</h3></th>
                        </tr>
                        <tr>
                            <td class="mediumWidth">
                                <strong>New User KYC Status</strong>
                            </td>
                            <td>
                                <gfs:select
                                        id="statusSelector"
                                        name="<%=UpdateUserKycStatusAction.Fields.newStatusOption%>"
                                        value=""
                                        list="<%=UpdateUserKycStatusAction.KycStatusUpdateOption.values()%>"
                                        listValue="<%=UpdateUserKycStatusAction.KycStatusUpdateOption.NAME_FOR_DISPLAY_PROPERTY%>"
                                        required="${true}"
                                />
                            </td>
                        </tr>
                        <tr>
                            <td class="mediumWidth">
                                <strong>Add Note</strong>
                            </td>
                            <td>
                                <gfs:text
                                        name="<%=UpdateUserKycStatusAction.Fields.note%>"
                                        value="${null}"
                                />
                            </td>
                        </tr>
                        <tr>
                            <td class="mediumWidth">
                                <gfs:submit
                                        value="Update User KYC Status"
                                />
                            </td>
                        </tr>
                    </table>
                </div>
            </gfs:ajaxForm>

            <br />
        </c:when>
    </c:choose>

    <div class="pageContentTopDescription">
        <table width="100%" style="margin-left: 20px; margin-right: 20px;">
            <tr>
                <th>
                    <h3>Events</h3>
                </th>
            </tr>
            <tr>
                <th class="mediumWidth">
                    <strong>Timestamp</strong>
                </th>
                <th class="mediumWidth">
                    <strong>Event Type</strong>
                </th>
                <th>
                    <strong>Actor Name</strong>
                </th>
                <th>
                    <strong>Note</strong>
                </th>
            </tr>

            <c:forEach var="event" items="${action.events}">
                <tr>
                    <td>${action.formatInstant(event.created)}</td>
                    <td>${event.type.nameForDisplay}</td>
                    <td>${event.actorDisplayName}</td>
                    <td>${event.note}</td>
                </tr>
            </c:forEach>

        </table>

    </div>

</div>
