<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%--
    jw: the purpose of this tag is to allow code to register end of page content to be included in the wrapper
        at the end of the page.  This usually happens with popup windows or other DHTML contracts where we want
        to include the non visible elements of a tool until the end of the page.  Things like file upload.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction" />
<g:ref var="global_delegateEndOfBodyContentToQSharkTopic" className="java.lang.Boolean" />
<g:ref var="global_insideEndOfBodyContent" className="java.lang.Boolean" />
<g:ref var="global_insideEndOfBodyContentContainer" className="java.lang.Boolean" />

<%--
    jw: some jsp may want to add some HTML to the end of the page, and that code may call tags which also want to
        add some HTML to the end of the page, so lets prevent nesting of this.  If we are already inside of it
        then just output the HTML and let the outer most call capture the result into the variable.
--%>
<c:choose>
    <c:when test="${global_insideEndOfBodyContent or (action.ajaxRequest and not global_insideEndOfBodyContentContainer)}">
        <jsp:doBody />
    </c:when>
    <c:otherwise>
        <g:set var="global_insideEndOfBodyContent" object="${true}" scope="request" className="java.lang.Boolean" />

        <%--
            this may seem strange but its necessary because if the variable isnt already defined then the reference
            within the g:set will cause a JSP resolution error
        --%>
        <c:if test="${g:isInPageContext(pageContext, 'global_endOfBodyContent','request')}">
            <g:ref var="global_endOfBodyContent" className="java.lang.String" />
        </c:if>

        <g:set var="global_endOfBodyContent" scope="request">
            ${global_endOfBodyContent}
            <jsp:doBody />
        </g:set>

        <g:set var="global_insideEndOfBodyContent" object="${false}" scope="request" className="java.lang.Boolean" />
    </c:otherwise>
</c:choose>

