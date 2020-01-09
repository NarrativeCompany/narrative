<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="role" required="true" type="org.narrative.network.shared.security.Role" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core"%>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy"%>

<ss:set var="user" object="${role.primaryRole.registeredUser ? role.user : null}" className="org.narrative.network.core.user.User" />

<c:choose>
    <%-- bl: if the user is visible or deleted, then just use/trust the display name resolved --%>
    <c:when test="${user.visible or user.deleted}">
        ${user.displayNameResolved}
    </c:when>
    <c:when test="${ss:exists(user)}">
        <%-- bl: non-visible, non-deleted user (disabled?), just treat as a guest --%>
        ${h:wordlet1Arg('role.customGuestName', user.displayNameResolved)}
    </c:when>
    <c:otherwise>
        ${role.displayNameResolved}
    </c:otherwise>
</c:choose>
