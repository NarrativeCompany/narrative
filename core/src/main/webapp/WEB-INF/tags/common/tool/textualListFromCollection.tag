<%--
  User: jonmark
  Date: 5/20/15
  Time: 1:11 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="texts" required="true" type="java.util.Collection" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>

<c:if test="${not empty texts}">
    <ss:forEach obj="text" items="${texts}" className="java.lang.String" varStatus="status">
        <c:if test="${not status.first}">
            <c:if test="${fn:length(texts)>2}">
                ${','}
            </c:if>
            <c:if test="${status.last}">
                ${' '}${h:wordlet('tags.common.tool.textualListFromCollection.and')}
            </c:if>
        </c:if>
        ${' '}
        ${text}
    </ss:forEach>
</c:if>