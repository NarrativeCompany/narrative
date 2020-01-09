<%--
  User: brian
  Date: 2019-06-12
  Time: 11:07
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="neoWallet" required="true" type="org.narrative.network.core.narrative.wallet.NeoWallet" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="action" className="org.narrative.network.shared.baseactions.NetworkAction"/>

<c:choose>
    <c:when test="${empty neoWallet.neoAddress}">
        <strong>???</strong>
    </c:when>
    <c:otherwise>
        <page:neoscanWalletLink address="${neoWallet.neoAddress}" />
    </c:otherwise>
</c:choose>
<br/>
${neoWallet.nameForDisplay}