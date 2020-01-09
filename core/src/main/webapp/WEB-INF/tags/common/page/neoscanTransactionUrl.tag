<%--
  User: brian
  Date: 2019-06-11
  Time: 09:04
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="transactionId" required="true" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<%-- bl: wrap in a set so that whitespace is trimmed --%>
<ss:set var="url">
    ${networkRegistry.neoscanBaseUrl}/transaction/${transactionId}
</ss:set>

${url}