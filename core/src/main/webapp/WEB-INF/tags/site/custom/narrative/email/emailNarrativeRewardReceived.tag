<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %><%--
  User: jonmark
  Date: 3/19/18
  Time: 2:38 PM
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

<ss:ref var="task" className="org.narrative.network.core.narrative.rewards.services.SendBulkRewardNotificationEmails"/>

<%-- bl: do not ask me why, but we have to use these variables or else resolution will not work.
     if you try ${task.networkContext.authZone} directly, it will NOT work below. --%>
<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>
<ss:set var="authZone" object="${networkContext.authZone}" className="org.narrative.network.core.user.AuthZone"/>

<ss:set var="reward" object="${task.reward}" className="org.narrative.network.customizations.narrative.NrveValue" />

<%-- bl: this has to use a JSTL expression, not a scriptlet, since the task object isn't available in scriptlet scope --%>
<ss:set var="rewardsUrl" object="${ReactRoute.USER_PROFILE_REWARDS.getUrl(task.getUser().getIdForUrl())}" />

<%-- todo:post-v1.3.2 remove this custom wording for july 2019 rewards --%>
<ss:set var="isJuly2019" object="${task.rewardPeriod.period.monthValue==7 and task.rewardPeriod.period.year==2019}" className="java.lang.Boolean" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailNarrativeRewardReceived.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailNarrativeRewardReceived.viewRewardDetails')}"
        actionUrl="${rewardsUrl}">
    <e:elt name="div" cssClass="<%=EmailCssClass.HEADER_IMAGE_CENTER%>">
        <e:img src="${authZone.baseUrl}/email/cersei-narrative.png" width="118"/>
    </e:elt>
    <e:elt name="h2" cssClass="<%=EmailCssClass.HEADER_IMAGE_CENTER%>">
        <c:choose>
            <%-- todo:post-v1.3.2 remove this --%>
            <c:when test="${isJuly2019}">
                You (really) rule!
            </c:when>
            <c:otherwise>
                ${h:wordlet('jsp.site.custom.narrative.email.emailNarrativeRewardReceived.youRule')}
            </c:otherwise>
        </c:choose>
    </e:elt>
    <e:elt name="p">
        ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailNarrativeRewardReceived.intro', reward.formattedWithEightDecimals, task.rewardPeriod.rewardYearMonth.rewardPeriodRange)}
    </e:elt>
    <%-- todo:post-v1.3.2 remove this --%>
    <c:if test="${isJuly2019}">
        <e:elt name="p">
            NOTE: This is a <e:elt name="strong">corrected</e:elt> reward statement. The previous email for July rewards had an incorrect amount.
            For more details, see the <e:a href="https://www.narrative.org/post/oops-july-rewards-to-be-updated">announcement</e:a>
            on the Narrative blog.
        </e:elt>
    </c:if>
</n_email:emailWrapper>

