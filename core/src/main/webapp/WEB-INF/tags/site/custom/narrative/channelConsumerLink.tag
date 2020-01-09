<%--
  User: jonmark
  Date: 2019-08-05
  Time: 11:13
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="consumer" required="true" type="org.narrative.network.customizations.narrative.channels.ChannelConsumer" %>
<%@ attribute name="expectedType" required="true" type="org.narrative.network.customizations.narrative.channels.ChannelType" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<c:choose>
    <c:when test="${ss:exists(consumer)}">
        <e:a href="${consumer.displayUrl}">
            ${consumer.nameForHtml}
        </e:a>
    </c:when>
    <c:otherwise>
        ${expectedType.deletedConsumerNameForDisplay}
    </c:otherwise>
</c:choose>