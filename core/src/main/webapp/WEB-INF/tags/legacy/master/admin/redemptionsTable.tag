<%--
  User: brian
  Date: 2019-06-11
  Time: 09:04
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="transactions" required="true" type="java.util.Collection" %>
<%@ attribute name="transactionTotal" required="true" type="org.narrative.network.customizations.narrative.NrveUsdValue" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<table class="data_table">
    <thead>
        <tr>
            <th>Date</th>
            <th>User</th>
            <th>Amount</th>
            <th>Wallet</th>
        </tr>
    </thead>
    <tbody>
        <ss:forEach items="${transactions}" obj="transaction" className="org.narrative.network.core.narrative.wallet.WalletTransaction">
            <tr>
                <td>${transaction.transactionDatetime}</td>
                <td>
                    <a href="${transaction.fromWallet.user.profileUrl}" target="_blank">${transaction.fromWallet.user.displayNameForHtml} @${transaction.fromWallet.user.username}</a>
                </td>
                <td>
                    ${transaction.nrveAmount.formattedWithEightDecimals} (${transaction.usdAmountAsGBigDecimal.formattedAsUsd})
                </td>
                <td>
                    <gma:neoWalletLinks address="${ss:exists(transaction.neoTransaction) ? transaction.neoTransaction.toNeoWallet.neoAddress : transaction.fromWallet.neoWallet.neoAddress}" />
                </td>
            </tr>
        </ss:forEach>
        <tr>
            <td>Total</td>
            <td>&nbsp;</td>
            <td>
                ${transactionTotal.nrve.formattedWithEightDecimals} (${transactionTotal.usd.formattedAsUsd})
            </td>
            <td>&nbsp;</td>
        </tr>
    </tbody>
</table>