<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%--
  User: jonmark
  Date: 6/3/13
  Time: 9:02 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="user" required="true" type="org.narrative.network.core.user.User" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<c:if test="${not user.userFields.emailVerified}">
    <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_PARAGRAPH%>">
        <e:a href="${user.userFields.emailAddress.primaryEmailConfirmationUrl}">${h:wordlet('tags.email.common.userVerificationLinks.pleaseClickThisLinkToVerify')}</e:a>
    </e:elt>
</c:if>
