<%@ page import="org.narrative.network.core.master.admin.actions.ApplyFiatAdjustmentAction" %>
<%@ page import="org.narrative.network.core.narrative.rewards.RewardPeriodStep" %>
<%--
  User: jonmark
  Date: 7/2/18
  Time: 1:54 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.ApplyFiatAdjustmentAction" />

<ss:set var="jobScheduledStep" object="<%=RewardPeriodStep.SCHEDULE_PROCESSING_JOB%>" className="org.narrative.network.core.narrative.rewards.RewardPeriodStep" />

<gct:wrapperCommonParams title="${action.rewardPeriod.rewardYearMonth.formatted} Fiat Adjustment" />

<ge:clusterWrapper>
    <p class="lgfont" style="margin-bottom: 20px;">
        Number of Fiat Payments in Month: <strong>${action.totalPaymentsPending}</strong>
    </p>
    <p class="lgfont" style="margin-bottom: 20px;">
        USD For NRVE: <strong>${action.totalUsdForNrve.formattedAsUsd}</strong>
    </p>
    <p class="lgfont" style="margin-bottom: 20px;">
        USD For Fees: <strong>${action.totalUsdForFees.formattedAsUsd}</strong>
    </p>
    <p class="lgfont" style="margin-bottom: 20px;">
        Total USD: <strong>${action.totalUsd.formattedAsUsd}</strong>
    </p>
    <p class="lgfont" style="margin-bottom: 20px;">
        Expected NRVE: <strong>${action.originalNrve.formattedWithSuffix}</strong>
    </p>
    <c:choose>
        <c:when test="${action.supportSubmissions}">
            <ss:set var="actionPath" object="<%=ApplyFiatAdjustmentAction.FULL_ACTION_PATH%>" className="java.lang.String" />
            <gfs:form id="applyFiatAdjustmentForm" action="${actionPath}!execute" method="post">
                <gfs:hidden name="<%=ApplyFiatAdjustmentAction.MONTH_FOR_ADJUSTMENT_PARAM%>" value="${action.rewardPeriod.period}" />
                <gfs:text
                        name="<%=ApplyFiatAdjustmentAction.ACQUIRED_NRVE_PARAM%>"
                        label="Acquired NRVE"
                        value=""
                        required="${true}"
                />
                <gfs:submit value="Apply Fiat Adjustment" />
            </gfs:form>
        </c:when>
        <c:when test="${not action.rewardPeriod.rewardYearMonth.beforeNow}">
            <gct:roundedDiv isStatusBubble="${true}">
                Processing must wait until next month.
            </gct:roundedDiv>
        </c:when>
        <c:otherwise>
            ${ss:assert(fn:contains(action.rewardPeriod.completedSteps, jobScheduledStep), 'Always expecting step 1 to have been completed at this point!')}
            <p class="lgfont" style="margin-bottom: 20px;">
                Actual (Adjusted) NRVE: <strong>${action.totalProratedRevenue.formattedWithSuffix}</strong>
            </p>
            <c:choose>
                <c:when test="${action.rewardPeriod.completed}">
                    <gct:roundedDiv isStatusBubble="${true}">
                        The Reward Period has completed processing.
                    </gct:roundedDiv>
                </c:when>
                <c:otherwise>
                    <gct:roundedDiv isStatusBubble="${true}">
                        The Reward Period still has to process the following steps. If processing has not yet begun, it will on the 6th of the month.
                        <ul>
                            <ss:forEach obj="step" items="<%=RewardPeriodStep.values()%>" className="org.narrative.network.core.narrative.rewards.RewardPeriodStep">
                                <c:if test="${not ss:contains(action.rewardPeriod.completedSteps, step)}">
                                    <li>${step}</li>
                                </c:if>
                            </ss:forEach>
                        </ul>
                    </gct:roundedDiv>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
</ge:clusterWrapper>