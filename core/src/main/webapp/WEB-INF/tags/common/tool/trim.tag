<%--
  User: brian
  Date: 5/6/16
  Time: 5:37 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>

<ss:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>

<%-- trim the output so it doesn't have spaces by setting it into a variable (which internally does the trim) --%>
<ss:set var="output">
    <jsp:doBody />
</ss:set>

${output}