<%--
  User: brian
  Date: Feb 4, 2008
  Time: 10:06:30 AM
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>

<ss:ref var="task" className="org.narrative.network.core.user.services.ResendEmailVerificationEmailTask"/>
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>
<ss:set var="subject" object="${h:wordlet('jsp.site.email.emailResendVerificationEmail.pleaseVerifyYourEmail')}" scope="request"/>

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.user.displayNameResolved}"
        actionUrl="${task.user.userFields.emailAddress.primaryEmailConfirmationUrl}"
        actionText="${h:wordlet('jsp.site.email.emailResendVerificationEmail.verifyYourEmail')}"
        includeGenericSalutation="${true}"
        includeSignature="${true}">

    <jsp:attribute name="introduction">
        ${h:wordlet('jsp.site.email.emailResendVerificationEmail.pleaseClickTheLinkToConfirm')}
    </jsp:attribute>

</email:emailWrapper>