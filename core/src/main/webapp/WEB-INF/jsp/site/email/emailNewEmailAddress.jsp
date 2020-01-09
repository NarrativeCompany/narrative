<%@ page import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%@ page import="org.narrative.network.shared.email.EmailCssClass" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.master.manage.profile.services.SendEmailChangedEmailTask" />
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />
<ss:set var="siteName" object="${task.networkContext.authZone.name}"/>
<%--to set the subject on the email --%>
<ss:set var="subject" object="${h:wordlet('jsp.site.email.emailNewEmailAddress.emailAddressChange')}" scope="request"/>

<ss:set var="emailAddress" object="${task.pendingEmailAddress}" className="org.narrative.network.core.user.EmailAddress" />
<ss:set var="forPrimaryEmailAddress" object="${task.forVerificationStep.verifyPrimary}" className="java.lang.Boolean" />

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.user.displayNameResolved}"
        actionUrl="${task.confirmationUrl}"
        actionText="${h:wordlet('jsp.site.email.emailNewEmailAddress.verifyEmailAddress')}"
        includeSignature="${true}">

    <jsp:attribute name="introduction">
        ${h:wordlet1Arg('jsp.site.email.emailNewEmailAddress.verifyYourEmailAddress', emailAddress.emailAddress)}
    </jsp:attribute>

    <jsp:attribute name="cardFooterHtml">
        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            <ss:set var="cancelLink">
                <e:a href="${task.cancelUrl}">
                    ${h:wordlet('jsp.site.email.emailNewEmailAddress.clickHereToCancel')}
                </e:a>
            </ss:set>
            <c:choose>
                <c:when test="${forPrimaryEmailAddress}">
                    <ss:set var="changePasswordLink">
                        <e:a href="<%=ReactRoute.MEMBER_ACCOUNT_SETTINGS.getUrl()%>">
                            ${h:wordlet('jsp.site.email.emailNewEmailAddress.goChangeYourPassword')}
                        </e:a>
                    </ss:set>
                    ${h:wordlet2Arg('jsp.site.email.emailNewEmailAddress.ifYouDidNotMakeChangeUpdatePassword', cancelLink, changePasswordLink)}
                </c:when>
                <c:otherwise>
                    ${h:wordlet1Arg('jsp.site.email.emailNewEmailAddress.ifYouDidNotMakeChange', cancelLink)}
                </c:otherwise>
            </c:choose>
        </e:elt>

        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            ${h:wordlet1Arg('jsp.site.email.emailNewEmailAddress.emailChangeExpiration', siteName)}
        </e:elt>
    </jsp:attribute>

</email:emailWrapper>
