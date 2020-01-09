<%@ page import="org.narrative.network.core.cluster.actions.ManageUserKycStatusAction" %>
<%@ page import="org.narrative.network.core.cluster.actions.UserKycStatusDetailsAction" %>
<%@ page import="org.narrative.network.core.cluster.services.UserKycList" %>
<%@ page import="org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="date" uri="http://www.narrative.org/tags/handy/date" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<ss:ref var="action" className="org.narrative.network.core.cluster.actions.ManageUserKycStatusAction"/>

<ss:set var="registry" object="${action.networkRegistry}" className="org.narrative.network.core.system.NetworkRegistry" />

<gct:wrapperCommonParams title="User KYC status results (${h:formatNumber(action.criteria.totalResultCount)})" />

<style>
    .mediumWidth {
        width: 200px;
    }
</style>

<ge:clusterWrapper>
    <ss:set var="actionPath" object="<%=ManageUserKycStatusAction.FULL_ACTION_PATH%>" className="java.lang.String" />
    <ss:set var="criteriaParam" object="<%=ManageUserKycStatusAction.CRITERIA_PARAM%>" className="java.lang.String" />

    <%-- jw: there are not enough parameters to worry about using a get request for this action --%>
    <gfs:form id="filterUserKycs" action="${action.networkRegistry.clusterCpRelativePath}${actionPath}" method="GET">
        <c:if test="${not empty action.actionErrors or not empty action.fieldErrors}">
            <gfs:formErrorMessages
                    formId="filterUserKycs"
                    actionErrors="${action.actionErrors}"
                    actionFieldErrors="${action.fieldErrors}"
            />
        </c:if>

        <gfs:select
                cssClass="mediumWidth"
                name="${criteriaParam}.kycStatus"
                value="${action.criteria.kycStatus}"
                list="<%=UserKycStatus.values()%>"
                listValue="<%=UserKycStatus.NAME_FOR_DISPLAY_PROPERTY%>"
                label="User KYC Status"
                extraValue="Any Status"/>

        <gfs:text
                cssClass="mediumWidth"
                name="${criteriaParam}.username"
                value="${action.criteria.username}"
                label="Handle"
        />

        <gfs:select
                cssClass="mediumWidth"
                name="${criteriaParam}.sortOrder"
                value="${action.criteria.sortOrder}"
                list="<%=UserKycList.SortOrder.values()%>"
                listValue="<%=UserKycList.SortOrder.NAME_FOR_DISPLAY_PROPERTY%>"
                label="Sort Order"
        />

        <gfs:submit value="Submit"/>
    </gfs:form>
    <br />
    <br />

    <c:if test="${not empty action.users}">
        <ss:forEach obj="userKyc" items="${action.users}" className="org.narrative.network.core.user.UserKyc">
            <ss:set var="user" object="${userKyc.user}" className="org.narrative.network.core.user.User" />
            <gct:roundedDiv>
                    <h3>
                        <strong class="small">
                            (${user.username})
                        </strong>
                    </h3>
                    <div class="small" >
                        <span style="padding-right: 20px">
                            <strong>Last Updated:</strong>
                            ${action.formatInstant(userKyc.lastUpdated)}
                        </span>
                        <span style="padding-right: 20px">
                            <strong>KYC Status:</strong>
                            <c:choose>
                                <c:when test="${userKyc.kycStatus.rejected}">
                                    <span style="color: red;">${userKyc.kycStatus.nameForDisplay}</span>
                                </c:when>
                                <c:otherwise>
                                    ${userKyc.kycStatus.nameForDisplay}
                                </c:otherwise>
                            </c:choose>
                        </span>
                        <c:if test="${userKyc.country != null}">
                            <span style="padding-right: 20px">
                                <strong>Country:</strong>
                                ${userKyc.country.nameForDisplay}
                            </span>
                        </c:if>
                        <a class="user-kyc-status-details" data-user-kyc-oid="${userKyc.oid}"><strong>Show Details</strong></a>
                    </div>
            </gct:roundedDiv>
        </ss:forEach>

        <ss:url id="paginationBaseUrl" value="${action.networkRegistry.clusterCpRelativePath}${actionPath}">
            <tool:addUrlParametersMap params="${action.criteriaParameters}" />
            <ss:param name="<%=ManageUserKycStatusAction.PAGE_PARAM%>" value="" />
        </ss:url>
        <gct:pagination
                currentPage="${action.page}"
                itemCount="${action.criteria.totalResultCount}"
                itemsPerPage="${action.criteria.maxResults}"
                urlLessPageNumber="${paginationBaseUrl}"
        />

        <%--
            USER DETAILS FROM KYC STATUS
        --%>
        <ss:set var="userKycSatusDetailsActionPath" object="<%=UserKycStatusDetailsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
        <gct:ajaxDivPopup id="userKycSatusDetails" title="KYC Status Details" width="900" url="${action.networkRegistry.clusterCpRelativePath}${userKycSatusDetailsActionPath}">
            <jsp:attribute name="initJavascriptBlock">
                <script type="text/javascript">
                  userKycSatusDetailsPopupWindow.addButton('userKycSatusDetailsCloseButton', 'Close', function() {
                    userKycSatusDetailsAjaxPopup.hide();
                  });
                </script>
            </jsp:attribute>
        </gct:ajaxDivPopup>
        <script type="text/javascript">
            jQuery('body').on('click.userKycSatusDetails', '.user-kyc-status-details', function(event) {
                var $link = jQuery(event.currentTarget);
                userKycSatusDetailsAjaxPopup.show(event.currentTarget, '/userKyc/'+$link.data('userKycOid'));
            });
        </script>
    </c:if>
</ge:clusterWrapper>


