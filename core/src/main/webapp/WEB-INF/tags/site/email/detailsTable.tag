<%@ tag import="org.narrative.network.shared.email.*" %>
<%--
  User: jonmark
  Date: 11/5/12
  Time: 8:47 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<e:elt name="table" cssClass="<%=EmailCssClass.DETAILS_BOX_TABLE%>">
    <jsp:doBody />
</e:elt>