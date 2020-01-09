<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%--
  User: jonmark
  Date: 11/5/12
  Time: 2:33 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="user" required="true" type="org.narrative.network.core.user.User" %>
<%@ attribute name="includeEmailAddress" type="java.lang.Boolean" %>
<%@ attribute name="emailAddressOverride" type="java.lang.String" %>
<%@ attribute name="userNameForDeletedUser" type="java.lang.String" %>
<%@ attribute name="ignoreUserVisibleFlag" type="java.lang.Boolean" %>
<%@ attribute name="afterDisplayNameText" type="java.lang.String" %>
<%@ attribute name="alternateUserUrl" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<email:detailsTable>
    <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
        <c:choose>
            <c:when test="${not empty userNameForDeletedUser}">${userNameForDeletedUser}</c:when>
            <c:when test="${not user.visible}">${user.displayNameResolved}</c:when>
            <c:otherwise>
                <%--<e:a href="${not empty alternateUserUrl ? alternateUserUrl : user.visible ? user.permalinkUrl : user.editProfileUrl}">${user.displayNameResolved}</e:a>--%>
                <s_page:displayName role="${user}" showSmallAvatarForEmail="${true}" />
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty afterDisplayNameText}">
            ${' '}${afterDisplayNameText}
        </c:if>
    </e:elt>

    <c:if test="${includeEmailAddress}">
        <e:elt name="div" cssClass="<%=EmailCssClass.DETAILS_BOX_CELL%>">
            <ss:set var="emailAddressResolved" object="${not empty emailAddressOverride ? emailAddressOverride : user.emailAddress}" className="java.lang.String" />
            <e:a href="mailto:${emailAddressResolved}">${emailAddressResolved}</e:a>
        </e:elt>
    </c:if>
</email:detailsTable>
