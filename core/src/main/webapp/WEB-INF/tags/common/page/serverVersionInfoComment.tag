<%--
  User: brian
  Date: 7/18/13
  Time: 8:36 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>

<ss:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<!--
VERSION: ${networkRegistry.version}
SERVER: ${networkRegistry.servletName}
CLUSTER: ${networkRegistry.clusterId}
-->