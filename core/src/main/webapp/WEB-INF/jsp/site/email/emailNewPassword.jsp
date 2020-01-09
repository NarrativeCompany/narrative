<%@ page import="org.narrative.network.shared.email.*" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.master.manage.profile.services.ChangePasswordTask" />
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />
<ss:set var="siteName" object="${task.networkContext.authZone.name}"/>
<%--to set the subject on the email --%>
<ss:set var="subject" object="${h:wordlet('jsp.site.email.emailNewPassword.yourPasswordHasChanged')}" scope="request"/>

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.user.displayNameResolved}"
        title="${h:wordlet('jsp.site.email.emailNewPassword.passwordChanged')}"
        includeSignature="${true}">

    <jsp:attribute name="introduction">
        ${h:wordlet('jsp.site.email.emailNewPassword.perYourRequestWeHaveChangedYourAccountPassword')}
    </jsp:attribute>

    <jsp:attribute name="information">
        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            ${h:wordlet1Arg('jsp.site.email.emailNewPassword.ifYouDidNotMakeChange', siteName)}
        </e:elt>
    </jsp:attribute>

</email:emailWrapper>
