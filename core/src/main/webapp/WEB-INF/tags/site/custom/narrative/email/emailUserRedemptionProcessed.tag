<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %><%--
  User: brian
  Date: 2019-07-02
  Time: 08:39
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.core.user.services.SendUserRedemptionProcessedEmail"/>
<ss:ref var="networkRegistry" className="org.narrative.network.core.system.NetworkRegistry"/>

<ss:set var="transaction" object="${task.transaction}" className="org.narrative.network.core.narrative.wallet.WalletTransaction" />

<%-- bl: there should only be one NeoTransactionId associated, so just grab the first item in the collection --%>
<ss:set var="neoTransactionId" object="${ss:firstItemInCol(transaction.neoTransaction.neoTransactionIds)}" className="org.narrative.network.core.narrative.wallet.NeoTransactionId" />

<ss:set var="neoTransactionUrl">
    <page:neoscanTransactionUrl transactionId="${neoTransactionId.transactionId}" />
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailUserRedemptionProcessed.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailUserRedemptionProcessed.viewTransaction')}"
        actionUrl="${neoTransactionUrl}">
    <ss:set var="neoAddressLink">
        <page:neoscanWalletLink address="${transaction.neoTransaction.toNeoWallet.neoAddress}" />
    </ss:set>

    <e:elt name="p">
        ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailUserRedemptionProcessed.toAddressMessage', transaction.nrveAmount.formattedWithSuffix, neoAddressLink)}
    </e:elt>

    <ss:set var="profileTransactionsLink" object="${ReactRoute.USER_PROFILE_REWARDS_TRANSACTIONS.getUrl(task.getUser().getIdForUrl())}" className="java.lang.String" />

    <ss:set var="rewardsTransactionsLink">
        <e:a href="${profileTransactionsLink}">
            ${h:wordlet('jsp.site.custom.narrative.email.emailUserRedemptionProcessed.rewardsTransactionsPage')}
        </e:a>
    </ss:set>

    <e:elt name="p">
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailUserRedemptionProcessed.viewInTransactions', rewardsTransactionsLink)}
    </e:elt>
</n_email:emailWrapper>