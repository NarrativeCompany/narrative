<%--
  User: jonmark
  Date: 4/17/14
  Time: 6:33 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="gmt" tagdir="/WEB-INF/tags/legacy/master" %>
<%@ taglib prefix="gta" tagdir="/WEB-INF/tags/legacy/area" %>

<g:set var="global_insideEndOfBodyContentContainer" object="${true}" scope="request" className="java.lang.Boolean" />

<jsp:doBody />

<g:ref var="global_endOfBodyContent" className="java.lang.String" />
${global_endOfBodyContent}
