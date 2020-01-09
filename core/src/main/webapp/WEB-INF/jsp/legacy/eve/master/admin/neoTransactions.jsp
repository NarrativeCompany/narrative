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
<%@ taglib prefix="gct" tagdir="/WEB-INF/tags/legacy/common" %>
<%@ taglib prefix="gfs" tagdir="/WEB-INF/tags/legacy/common/forms/simple" %>
<%@ taglib prefix="ge" tagdir="/WEB-INF/tags/legacy/eve" %>
<%@ taglib prefix="gma" tagdir="/WEB-INF/tags/legacy/master/admin" %>

<ss:ref var="action" className="org.narrative.network.core.master.admin.actions.NeoTransactionsAction" />

<ss:set var="actionPath" object="<%=NeoTransactionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="completedActionPath" object="<%=CompletedNeoTransactionsAction.FULL_ACTION_PATH%>" className="java.lang.String" />
<ss:set var="formType" object="<%=NeoTransactionsAction.FORM_TYPE%>" className="java.lang.String" />

<gct:wrapperCommonParams title="NEO Transactions" />

<ge:clusterWrapper>
    <h4><a href="${completedActionPath}">View Completed Transactions</a></h4>
    <gfs:form id="${formType}" action="${actionPath}!execute" method="post">
        <table class="data_table">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>From Wallet</th>
                    <th>To Wallet</th>
                    <th>NRVE Amount</th>
                    <th>Transaction ID</th>
                </tr>
            </thead>
            <tbody>
                <ss:forEach items="${action.neoTransactions}" obj="neoTransaction" className="org.narrative.network.core.narrative.wallet.NeoTransaction">
                    <ss:set var="transactionIds" object="${action.transactionToTransactionId[neoTransaction]}" className="java.util.List" />
                    <%-- bl: make sure we show at least one transaction ID input! --%>
                    <c:if test="${empty transactionIds}">
                        <ss:set var="transactionIds" object="${ss:newList()}" className="java.util.List" />
                        ${ss:collectionAdd(transactionIds, '')}
                    </c:if>
                    <ss:set var="fieldName" object="transactionToTransactionId['${neoTransaction.oid}']" className="java.lang.String" />
                    <ss:set var="fieldId" object="transactionId_${neoTransaction.oid}" className="java.lang.String" />

                    <tr>
                        <td>
                            <gfs:controlwrapper
                                    id="${fieldId}"
                                    name="${fieldName}"
                                    label="${neoTransaction.type}"
                                    required="${null}"
                                    cssClass="noLabel form_css"
                                    labelCssStyle="position: static;"
                            />
                        </td>
                        <td>
                            <gma:neoWalletDisplayName neoWallet="${neoTransaction.fromNeoWallet}"/>
                        </td>
                        <td>
                            <gma:neoWalletDisplayName neoWallet="${neoTransaction.toNeoWallet}"/>
                        </td>
                        <td>
                            ${neoTransaction.nrveAmount.formattedWithEightDecimals}
                        </td>
                        <td id="transactionIdCol${neoTransaction.oid}" style="white-space: nowrap">
                            <ss:forEach items="${transactionIds}" obj="transactionId" className="java.lang.String" varStatus="varStatus">
                                <gma:neoTransactionIdInput
                                        fieldName="${fieldName}"
                                        transactionId="${transactionId}"
                                        isDisabled="${empty neoTransaction.fromNeoWallet.neoAddress or empty neoTransaction.toNeoWallet.neoAddress}">
                                    <jsp:attribute name="afterInputHtml">
                                        <c:if test="${varStatus.first}">
                                            <a href="javascript:void(0);" onclick="Narrative.templateAppend('addNewTransactionIdTemplateId', 'transactionIdCol${neoTransaction.oid}', {fieldName: ${ss:escapeQuotes(ss:getJavascriptStringValue(fieldName))}});">Add</a>
                                        </c:if>
                                    </jsp:attribute>
                                </gma:neoTransactionIdInput>
                            </ss:forEach>
                        </td>
                    </tr>
                </ss:forEach>
            </tbody>
        </table>

        <gfs:submit value="Submit" />
    </gfs:form>

    <textarea id="addNewTransactionIdTemplateId" style="display:none;">
        <gma:neoTransactionIdInput
                transactionId=""
                fieldName="{fieldName}"
        />
    </textarea>
</ge:clusterWrapper>