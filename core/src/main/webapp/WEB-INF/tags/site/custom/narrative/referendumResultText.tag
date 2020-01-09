<%--
  User: jonmark
  Date: 4/3/18
  Time: 10:10 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="referendum" required="true" type="org.narrative.network.customizations.narrative.niches.referendum.Referendum" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>

<c:choose>
    <c:when test="${referendum.type.tribunalReferendum}">
        <narrative:tribunalReferendumResultText referendum="${referendum}"/>
    </c:when>
    <c:otherwise>
        <ss:set var="resultSuffix" object="${referendum.resultWordletSuffix}" className="java.lang.String" />
        ${h:wordlet('tags.site.custom.narrative.referendumResultText.resultDescription'+=resultSuffix)}
    </c:otherwise>
</c:choose>
