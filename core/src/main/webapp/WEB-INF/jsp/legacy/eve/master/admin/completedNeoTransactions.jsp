<%@ page import="org.narrative.network.core.master.admin.actions.NeoTransactionsAction" %>
<%@ page import="org.narrative.network.core.master.admin.actions.CompletedNeoTransactionsAction" %>
<%--
  User: brian
  Date: 2019-06-12
  Time: 09:13
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="gn" uri="http://legacy.narrative.org/tags/network"%>
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.CompletedNeoTransactionsAction" />
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry" />

<ss:set var="actionPath" object="<%=CompletedNeoTransactionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="incompleteActionPath" object="<%=NeoTransactionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="pageParam" object="<%=CompletedNeoTransactionsAction.PAGE_PARAM%>" className="java.lang.String" />
<ss:set var="completedRowsPerPage" object="<%=CompletedNeoTransactionsAction.COMPLETED_ROWS_PER_PAGE%>" className="java.lang.Integer" />

<gct:wrapperCommonParams title="Completed NEO Transactions" />

<ge:clusterWrapper>
    <h4><a href="${incompleteActionPath}">View Pending Transactions</a></h4>
    <table class="data_table">
        <thead>
            <tr>
                <th>Date</th>
                <th>Type</th>
                <th>From</th>
                <th>To</th>
                <th>Amount</th>
                <th>Transaction ID</th>
            </tr>
        </thead>
        <tbody>
            <ss:forEach items="${action.neoTransactions}" obj="neoTransaction" className="org.narrative.network.core.narrative.wallet.NeoTransaction">
                <tr>
                    <td>
                        <ss:forEach items="${neoTransaction.neoTransactionIds}" obj="neoTransactionId" className="org.narrative.network.core.narrative.wallet.NeoTransactionId" varStatus="varStatus">
                            <c:if test="${not varStatus.first}"><br/></c:if>
                            ${neoTransactionId.transactionDatetime}
                        </ss:forEach>
                    </td>
                    <td>${neoTransaction.type}</td>
                    <td>
                        <gma:neoWalletDisplayName neoWallet="${neoTransaction.fromNeoWallet}"/>
                    </td>
                    <td>
                        <gma:neoWalletDisplayName neoWallet="${neoTransaction.toNeoWallet}"/>
                    </td>
                    <td>${neoTransaction.nrveAmount.formattedWithSuffix}</td>
                    <td>
                        <ss:forEach items="${neoTransaction.neoTransactionIds}" obj="neoTransactionId" className="org.narrative.network.core.narrative.wallet.NeoTransactionId" varStatus="varStatus">
                            <c:if test="${not varStatus.first}"><br/></c:if>
                            <page:neoscanTransactionLink transactionId="${neoTransactionId.transactionId}" />
                        </ss:forEach>
                    </td>
                </tr>
            </ss:forEach>
        </tbody>
    </table>

    <gct:pagination
            currentPage="${action.page}"
            itemCount="${action.countCompletedTransactions}"
            itemsPerPage="${completedRowsPerPage}"
            urlLessPageNumber="${actionPath}?${pageParam}="
    />
</ge:clusterWrapper>