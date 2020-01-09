<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %><%--
  User: jonmark
  Date: 3/19/18
  Time: 2:38 PM
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

<ss:ref var="task" className="org.narrative.network.core.narrative.rewards.services.SendProratedRefundProcessedEmail"/>

<ss:set var="invoice" object="${task.invoice}" className="org.narrative.network.customizations.narrative.invoices.Invoice" />
<ss:set var="invoiceConsumer" object="${invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.invoices.InvoiceConsumer" />
<ss:set var="purchasePayment" object="${invoice.purchasePaymentResolved}" className="org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase" />
<ss:set var="refundTransaction" object="${purchasePayment.refundWalletTransaction}" className="org.narrative.network.core.narrative.wallet.WalletTransaction" />

<%-- bl: this has to use a JSTL expression, not a scriptlet, since the task object isn't available in scriptlet scope --%>
<ss:set var="rewardsTransactionsUrl" object="${ReactRoute.USER_PROFILE_REWARDS_TRANSACTIONS.getUrl(task.getUser().getIdForUrl())}" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailProratedRefundProcessed.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailProratedRefundProcessed.viewTransactions')}"
        actionUrl="${rewardsTransactionsUrl}">
    <e:elt name="p">
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailProratedRefundProcessed.intro', refundTransaction.nrveAmount.formattedWithEightDecimals)}
        <c:if test="${purchasePayment.nrveAmount != refundTransaction.nrveAmount}">
            ${' '}${h:wordlet1Arg('jsp.site.custom.narrative.email.emailProratedRefundProcessed.amountProrated', invoiceConsumer.invoiceConsumerTypeName)}
        </c:if>
    </e:elt>
    <e:elt name="p">
        <ss:set var="consumerLink">
            <e:a href="${invoiceConsumer.consumerDisplayUrl}">
                ${invoiceConsumer.consumerDisplayName}
            </e:a>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailProratedRefundProcessed.description.' += invoice.type, consumerLink)}
    </e:elt>
</n_email:emailWrapper>

