<%--
  User: jonmark
  Date: 3/3/15
  Time: 12:49 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="consumer" required="true" type="org.narrative.network.core.composition.base.CompositionConsumer" %>
<%@ attribute name="linkSubject" type="java.lang.Boolean" %>

<%@ attribute name="reply" type="org.narrative.network.core.composition.base.Reply" %>
<%@ attribute name="replyOid" type="java.lang.Object" %>
<%@ attribute name="lastReply" type="java.lang.Boolean" %>
<%@ attribute name="firstUnread" type="java.lang.Boolean" %>
<%@ attribute name="targetBlank" type="java.lang.Boolean" %>
<%@ attribute name="cssClasses" type="java.lang.String" %>
<%@ attribute name="itemProp" type="java.lang.String" %>
<%@ attribute name="anchorToTopOfPage" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:set var="linkText">
    <c:choose>
        <c:when test="${linkSubject}">${consumer.titleForDisplay}</c:when>
        <c:otherwise><jsp:doBody /></c:otherwise>
    </c:choose>
</ss:set>
<ss:set var="contentUrl">
    <s_page:compositionConsumerUrl anchorToTopOfPage="${anchorToTopOfPage}" consumer="${consumer}" reply="${reply}" replyOid="${replyOid}" lastReply="${lastReply}" firstUnread="${firstUnread}" />
</ss:set>

<%-- bl: add rel="nofollow" to all of our direct reply links. --%>
<e:a href="${contentUrl}" noFollow="${firstUnread or lastReply or ss:exists(reply) or replyOid ne null}" targetBlank="${targetBlank}" cssClassNames="${cssClasses}" itemProp="${itemProp}">${linkText}</e:a>
