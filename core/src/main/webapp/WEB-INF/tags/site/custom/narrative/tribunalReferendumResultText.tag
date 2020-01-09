<%--
  User: martin
  Date: 9/4/18
  Time: 2:05 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="referendum" required="true" type="org.narrative.network.customizations.narrative.niches.referendum.Referendum" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="dom" tagdir="/WEB-INF/tags/common/dom" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>

${ss:assert(not referendum.open and referendum.type.tribunalReferendum, 'To get the result text, closed tribunal referendum must be passed')}

<ss:set var="resultWordlet" object="tags.site.custom.narrative.tribunalReferendumResultText${referendum.resultWordletSuffix}"/>

<c:choose>
    <c:when test="${referendum.wasUnanimousTribunalVote}">
        <%--mk: unanimous result--%>
        ${h:wordlet(resultWordlet)}
    </c:when>
    <c:otherwise>
        <%--mk: non-unanimous affirmed status result--%>
        ${h:wordlet1Arg(resultWordlet, referendum.wasPassed ? referendum.approvalPercentage.formattedAsTwoDecimalPercentage : referendum.againstPercentage.formattedAsTwoDecimalPercentage)}
    </c:otherwise>
</c:choose>