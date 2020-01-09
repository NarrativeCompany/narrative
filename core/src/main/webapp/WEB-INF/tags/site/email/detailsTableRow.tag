<%@ tag import="org.narrative.network.shared.email.*" %>
<%--
  User: jonmark
  Date: 11/5/12
  Time: 8:55 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="title" type="java.lang.String" %>
<%@ attribute name="extraCssClass" type="org.narrative.network.shared.email.EmailCssClass" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl"/>
<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext" />

<ss:set var="cssClasses" object="${ss:newSet()}" className="java.util.Collection" />
<ss:set var="inlineInfoClass" object="<%=EmailCssClass.DETAILS_BOX_INFO%>" className="org.narrative.network.shared.email.EmailCssClass" />
${ss:collectionAdd(cssClasses, inlineInfoClass)}
<c:if test="${extraCssClass ne null}">
    ${ss:collectionAdd(cssClasses, extraCssClass)}
</c:if>

<e:elt name="tr" cssClass="<%=EmailCssClass.DETAILS_BOX_ROW%>">
    <e:elt name="td" cssClass="<%=EmailCssClass.DETAILS_BOX_TITLE%>">
        <c:choose>
            <c:when test="${not empty title}">
                ${title}
            </c:when>
            <c:otherwise>
                &nbsp;
            </c:otherwise>
        </c:choose>
    </e:elt>
    <e:elt name="td" cssClasses="${cssClasses}">
        <jsp:doBody />
    </e:elt>
</e:elt>
