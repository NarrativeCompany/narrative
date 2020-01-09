<%--
  User: jonmark
  Date: 3/26/18
  Time: 7:33 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="referendum" required="true" type="org.narrative.network.customizations.narrative.niches.referendum.Referendum" %>
<%@ attribute name="dontLink" type="java.lang.Boolean" %>
<%@ attribute name="lowercase" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="dom" tagdir="/WEB-INF/tags/common/dom" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:set var="suffix" object="${lowercase ? '.lc' : ''}${referendum.type.tribunalReferendum ? '.forTribunal' : ''}" className="java.lang.String" />
<c:choose>
    <c:when test="${dontLink}">
        ${h:wordlet('tags.site.custom.narrative.referendumTitle.title'+=suffix)}
    </c:when>
    <c:otherwise>
        ${ss:assert(not lowercase, 'Currently do not have wordlets configured for lowercase linked referendum titles!')}
        <e:a href="${referendum.detailsUrl}">
            ${h:wordlet('tags.site.custom.narrative.referendumTitle.linkedTitle'+=suffix)}
        </e:a>
    </c:otherwise>
</c:choose>