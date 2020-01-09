<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%--
  User: jonmark
  Date: 3/12/15
  Time: 3:34 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="role" required="true" type="org.narrative.network.shared.security.Role" %>
<%@ attribute name="targetBlank" type="java.lang.Boolean" %>
<%@ attribute name="extraCssClasses" type="java.lang.String" %>
<%@ attribute name="includeNonDefaultTitleOnly" type="java.lang.Boolean" %>
<%@ attribute name="showSmallAvatarForEmail" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="dom" tagdir="/WEB-INF/tags/common/dom" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<ss:set var="areaContext" object="${h:areaContext(pageContext)}" className="org.narrative.network.shared.context.AreaContext" />

<ss:set var="user" object="${role.primaryRole.registeredUser ? role.user : null}" className="org.narrative.network.core.user.User" />

<ss:ref var="isJspEmail" className="java.lang.Boolean" />

<c:choose>
    <c:when test="${user.visible}">
        <e:a href="${user.permalinkUrl}">${user.displayNameResolved}</e:a>

        <c:if test="${isJspEmail and showSmallAvatarForEmail}">
            <s_page:avatarForUser user="${user}" imageDimensionOverride="20" emailCssClass="<%=EmailCssClass.DETAILS_BOX_SMALL_AVATAR%>" />
        </c:if>
    </c:when>
    <c:otherwise>
        <s_page:displayNameText role="${role}" />
    </c:otherwise>
</c:choose>