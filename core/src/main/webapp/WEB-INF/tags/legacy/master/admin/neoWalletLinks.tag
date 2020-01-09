<%--
  User: brian
  Date: 2019-06-11
  Time: 09:04
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="address" required="true" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<page:neoscanWalletLink address="${address}">NEOSCAN</page:neoscanWalletLink>
<c:if test="${networkRegistry.usesNeoMainNet}">
    &middot;
    <a href="https://explorer.o3.network/addresses/${address}" target="_blank">O3</a>
</c:if>
&middot;
<a href="${networkRegistry.neotrackerBaseUrl}/address/${address}" target="_blank">NEO Tracker</a>
&middot;
<a href="${networkRegistry.newEconoLabBaseUrl}/address/${address}" target="_blank">NewEconoLabs</a>