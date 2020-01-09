<%@ page import="org.narrative.network.core.master.admin.actions.NeoWalletsAction" %>
<%--
  User: brian
  Date: 2019-06-11
  Time: 09:13
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.NeoWalletsAction" />

<ss:set var="actionPath" object="<%=NeoWalletsAction.FULL_ACTION_PATH%>" className="java.lang.String" />

<gct:wrapperCommonParams title="NEO Wallets" />

<ge:clusterWrapper>
    <gfs:form id="updateNeoWalletAddressesForm" action="${actionPath}!execute" method="post">
        <table class="data_table">
            <thead>
                <tr>
                    <th>Wallet</th>
                    <th>Address</th>
                    <th>Expected Balance</th>
                    <th>Links</th>
                </tr>
            </thead>
            <tbody>
                <ss:forEach items="${action.neoWallets}" obj="neoWallet" className="org.narrative.network.core.narrative.wallet.NeoWallet">
                    <ss:set var="neoAddress" object="${action.walletToNeoAddress[neoWallet]}" className="java.lang.String" />
                    <ss:set var="expectedBalance" object="${neoWallet.expectedBalance}" className="org.narrative.network.customizations.narrative.NrveValue" />

                    <tr>
                        <td>${neoWallet.nameForDisplay}</td>
                        <td>
                            <%-- bl: don't allow addresses to change for now (will require more work if we want to actually support this; see #3325). --%>
                            <gfs:text
                                    name="walletToNeoAddress['${neoWallet.oid}']"
                                    value="${neoAddress}"
                                    size="60"
                                    disabled="${not empty neoAddress}"
                            />
                        </td>
                        <td>
                            <c:if test="${expectedBalance != null}">
                                ${expectedBalance.formattedWithSuffix}
                            </c:if>
                        </td>
                        <td>
                            <gma:neoWalletLinks address="${neoAddress}" />
                        </td>
                    </tr>
                </ss:forEach>
            </tbody>
        </table>

        <gfs:submit value="Submit" />
    </gfs:form>
</ge:clusterWrapper>