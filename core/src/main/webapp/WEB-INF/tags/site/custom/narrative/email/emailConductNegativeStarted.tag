<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.reputation.services.SendConductNegativeStartedEmailTask"/>

<%-- bl: this has to use a JSTL expression, not a scriptlet, since the task object isn't available in scriptlet scope --%>
<ss:set var="reputationUrl" object="${ReactRoute.USER_PROFILE_REPUTATION.getUrl(task.getUser().getIdForUrl())}" />
<ss:set var="certificationUrl" object="<%=ReactRoute.MEMBER_CERTIFICATION.getUrl()%>" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.viewStatus')}"
        actionUrl="${reputationUrl}">

    <e:elt name="h1">
        <ss:set var="redNegative">
            <e:elt name="span" cssClass="<%=EmailCssClass.RED_TEXT%>">
                ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.negative')}
            </e:elt>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeStarted.heading', redNegative)}
    </e:elt>
    <e:elt name="p">
        <ss:set var="reason">
            <c:choose>
                <c:when test="${task.conductEventType.aupViolation}">
                    <ss:set var="aupLink">
                        <e:a href="${task.networkContext.networkRegistry.acceptableUsePolicyUrl}">${h:wordlet('conductEventType.emailReason.aup')}</e:a>
                    </ss:set>
                    ${h:wordlet1Arg('conductEventType.emailReason.' += task.conductEventType, aupLink)}
                </c:when>
                <c:otherwise>
                    ${h:wordlet('conductEventType.emailReason.' += task.conductEventType)}
                </c:otherwise>
            </c:choose>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeStarted.intro', reason)}
    </e:elt>

    <e:elt name="h2">
        ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.heading2')}
    </e:elt>
    <e:elt name="p">
        <ss:set var="redConductNegative">
            <e:elt name="span" cssClass="<%=EmailCssClass.RED_TEXT%>">
                ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.conductNegative')}
            </e:elt>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeStarted.meaning', redConductNegative)}
    </e:elt>
    <e:elt name="ul">
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul1')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul2')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul3')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul4')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul5')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul6')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul7')}
        </e:elt>
        <e:elt name="li">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ul8')}
        </e:elt>
    </e:elt>

    <e:elt name="p">
        <ss:set var="conductNegativeExpirationDatetime">
            <comp:datetime datetime="${task.conductNegativeExpirationDatetime}" isLongFormat="${true}" isNoPrettyTime="${true}" />
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeStarted.durationOfConductNegative', conductNegativeExpirationDatetime)}
    </e:elt>

    <c:if test="${not task.user.userKyc.kycStatus.approved}">
        <ss:set var="certifiedLink">
            <e:a href="${certificationUrl}">${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.certified')}</e:a>
        </ss:set>
        <e:elt name="h2">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.heading3')}
        </e:elt>
        <e:elt name="p">
            ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.fix')}
        </e:elt>
        <e:elt name="ol">
            <e:elt name="li">
                ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeStarted.ol1')}
            </e:elt>
            <e:elt name="li">
                ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeStarted.ol2', certifiedLink)}
            </e:elt>
        </e:elt>
    </c:if>
</n_email:emailWrapper>
