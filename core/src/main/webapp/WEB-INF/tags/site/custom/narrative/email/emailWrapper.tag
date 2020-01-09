<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %><%--
  User: jonmark
  Date: 2/21/18
  Time: 9:57 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="subject" required="true" type="java.lang.String" %>
<%@ attribute name="actionText" type="java.lang.String" %>
<%@ attribute name="actionUrl" type="java.lang.String" %>
<%@ attribute name="extraDetails" type="java.lang.String" %>
<%@ attribute name="details" type="java.lang.String" %>
<%@ attribute name="information" type="java.lang.String" %>
<%@ attribute name="afterBody" type="java.lang.String" %>
<%@ attribute name="cardFooterHtml" type="java.lang.String" %>
<%@ attribute name="watchedChannelConsumer" type="org.narrative.network.customizations.narrative.channels.ChannelConsumer" %>

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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.services.SendNarrativeEmailTaskBase" />

<%-- jw: set the subject into request scope so that it will be associated to the email! --%>
<ss:set var="subject" object="${subject}" className="java.lang.String" scope="request" />

<email:emailWrapper
        subject="${subject}"
        salutationDisplayName="${task.user.displayNameForHtml}"
        actionText="${actionText}"
        actionUrl="${actionUrl}"
        dontAllowUnsubscribe="${task.alwaysSendEmail}"
        extraDetails="${extraDetails}"
        details="${details}"
        watchedChannelConsumer="${watchedChannelConsumer}"
        information="${information}"
        cardFooterHtml="${cardFooterHtml}">
    <jsp:attribute name="introduction">
        <jsp:doBody />
    </jsp:attribute>
    <jsp:body>
        <c:if test="${not empty afterBody}">
            <e:elt name="div" cssClass="<%=EmailCssClass.INTRO_PARAGRAPH%>">
                ${afterBody}
            </e:elt>
        </c:if>
    </jsp:body>
</email:emailWrapper>