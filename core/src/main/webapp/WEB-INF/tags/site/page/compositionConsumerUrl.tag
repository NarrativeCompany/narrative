<%@ tag import="org.narrative.network.core.composition.base.Composition" %>
<%@ tag import="org.narrative.network.core.composition.base.CompositionType" %>
<%@ tag import="org.narrative.network.core.content.base.actions.*" %>
<%--
  User: jonmark
  Date: 3/3/15
  Time: 12:38 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="consumer" required="true" type="org.narrative.network.core.composition.base.CompositionConsumer" %>
<%@ attribute name="reply" type="org.narrative.network.core.composition.base.Reply" %>
<%@ attribute name="replyOid" type="java.lang.Object" %>
<%@ attribute name="lastReply" type="java.lang.Boolean" %>
<%@ attribute name="firstUnread" type="java.lang.Boolean" %>
<%@ attribute name="permalink" type="java.lang.Boolean" %>
<%@ attribute name="anchorToTopOfPage" type="java.lang.Boolean" %>
<%@ attribute name="extraParams" type="java.util.Map" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<ss:ref var="contextHolder" className="org.narrative.network.shared.context.NetworkContextHolder" />

<ss:ref var="isJspEmail" className="java.lang.Boolean" />

${ss:assert(not anchorToTopOfPage or replyOid eq null, 'topOfPage and replyOid cannot be used together')}

<c:if test="${ss:exists(reply) and replyOid eq null}">
    <ss:set var="replyOid" object="${reply.oid}" className="java.lang.Object" />
</c:if>

<ss:set var="replyParamName">
    <c:choose>
        <c:when test="${isJspEmail}">
            <%=CompositionType.COMMENT_PARAM_NAME%>
        </c:when>
        <c:otherwise>
            reply
        </c:otherwise>
    </c:choose>
</ss:set>

<ss:url id="consumerUrl" value="${permalink ? consumer.permalinkUrl : consumer.displayUrl}">
    <tool:addUrlParametersMap params="${extraParams}" />
    <c:choose>
        <c:when test="${replyOid ne null}">
            <ss:set var="anchorValue" object="${replyOid}" className="java.lang.Object" />
            <ss:param name="${replyParamName}" value="${replyOid}" />
        </c:when>
        <c:when test="${lastReply}">
            <ss:set var="anchorValue" object="<%=Composition.LAST_REPLY%>" className="java.lang.String" />
            <ss:param name="${replyParamName}" value="<%=Composition.LAST_REPLY%>" />
        </c:when>
        <c:when test="${firstUnread}">
            <ss:param name="${replyParamName}" value="<%=Composition.UNREAD_REPLY%>" />
        </c:when>
        <c:when test="${anchorToTopOfPage}">
            <ss:set var="anchorValue" object="topOfPage"/>
        </c:when>
    </c:choose>
</ss:url>

<c:choose>
    <c:when test="${not empty anchorValue}">${consumerUrl}#${anchorValue}</c:when>
    <c:otherwise>${consumerUrl}</c:otherwise>
</c:choose>