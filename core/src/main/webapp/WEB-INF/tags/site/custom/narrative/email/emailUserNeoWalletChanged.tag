<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %><%--
  User: jonmark
  Date: 3/19/18
  Time: 11:36 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.user.services.SendUserNeoWalletChangedEmail"/>
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<ss:set var="neoWallet" object="${task.user.wallet.neoWallet}" className="org.narrative.network.core.narrative.wallet.NeoWallet" />
<ss:set var="forRemoval" object="${not ss:exists(neoWallet)}" className="java.lang.Boolean" />
<ss:set var="wordletSuffix" object="${forRemoval ? '.forRemoval' : task.hadPreviousAddress ? '.forUpdate' : ''}" className="java.lang.String" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailUserNeoWalletChanged.subject'+=wordletSuffix)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailUserNeoWalletChanged.viewWallet')}"
        actionUrl="<%=ReactRoute.MEMBER_NEO_WALLET.getUrl()%>">
    <c:choose>
        <c:when test="${not forRemoval}">
            <ss:set var="neoAddressLink">
                <page:neoscanWalletLink address="${neoWallet.neoAddress}" />
            </ss:set>

            <e:elt name="p">
                ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailUserNeoWalletChanged.toAddressMessage'+=wordletSuffix, neoAddressLink)}
            </e:elt>

            <e:elt name="p">
                ${h:wordlet('jsp.site.custom.narrative.email.emailUserNeoWalletChanged.changeWarning')}
            </e:elt>
        </c:when>
        <c:otherwise>
            <e:elt name="p">
                ${h:wordlet('jsp.site.custom.narrative.email.emailUserNeoWalletChanged.unsetMessage')}
            </e:elt>
        </c:otherwise>
    </c:choose>

</n_email:emailWrapper>

