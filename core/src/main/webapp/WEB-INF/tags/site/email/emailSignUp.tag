<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%--
  User: jonmark
  Date: 6/23/17
  Time: 9:48 AM
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
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.user.services.SendNewUserEmailTask"/>
<ss:ref var="networkContext" className="org.narrative.network.shared.context.NetworkContext" />
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<ss:set var="subject" object="${h:wordlet1Arg('tags.site.email.emailSignUp.welcomeTo', task.user.authZone.name)}" scope="request" />

<ss:set var="sandboxedUser" object="${task.user.sandboxedAreaUser}" className="org.narrative.network.core.area.user.SandboxedAreaUser" />

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.user.displayNameForHtml}"
        title="${h:wordlet('tags.site.email.emailSignUp.yourNewAccount')}"
        includeSignature="${true}">

    <jsp:attribute name="details">
        <email:userDetailsTable user="${task.user}" includeEmailAddress="${true}" />
    </jsp:attribute>

    <jsp:attribute name="information">
        <email:userVerificationLinks user="${task.user}" />
    </jsp:attribute>

</email:emailWrapper>
