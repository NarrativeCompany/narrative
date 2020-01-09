<%--
  User: brian
  Date: Nov 8, 2010
  Time: 9:06:38 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="formId" required="true" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="g" uri="http://legacy.narrative.org/tags/core" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>

<g:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>

<%-- bl: use the formId as the formType.  we'll only display errors if the current formType is this form --%>
<gfs:hidden name="formType" value="${formId}" />