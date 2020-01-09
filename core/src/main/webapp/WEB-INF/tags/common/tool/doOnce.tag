<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="id" required="true" description="The id of the code to execute only a single time" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core"%>

<%-- bl: need to use request scope in order for this to work correctly.  even within the context
     of the same JSP invocation, the pageContext seems to change on a per tag-file basis.  not quite
     how I understood page vs. request scope to work. --%>
<c:if test="${!ss:isInPageContext(pageContext,id,'request')}">
    <ss:varVarSet var="${id}" object="${true}" scope="request" />
    <jsp:doBody />
</c:if>
