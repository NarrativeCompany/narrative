<%@ page import="org.narrative.network.shared.email.*" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>



<ss:ref var="task" className="org.narrative.network.core.master.manage.profile.services.SendLostPasswordEmailTask" />

<%--to set the subject on the email --%>
<ss:set var="subject" object="${h:wordlet('jsp.site.email.emailLostPassword.resetPasswordRequest')}" scope="request"/>

<email:emailWrapper
        subject="${subject}"
        title="${h:wordlet('jsp.site.email.emailLostPassword.resetPasswordRequest')}"
        includeSignature="${true}">

    <jsp:attribute name="introduction">
        ${h:wordlet1Arg('jsp.site.email.emailLostPassword.weHaveReceivedResetRequest', task.user.emailAddress)}
    </jsp:attribute>

    <jsp:attribute name="information">
        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            ${h:wordlet('jsp.site.email.emailLostPassword.clickToResetPassword')}
        </e:elt>

        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            <e:a href="${task.resetPasswordUrlForUser}">${task.resetPasswordUrlForUser}</e:a>
        </e:elt>
        ${h:wordlet('jsp.site.email.emailLostPassword.linkValidForOneHour')}
    </jsp:attribute>

</email:emailWrapper>
