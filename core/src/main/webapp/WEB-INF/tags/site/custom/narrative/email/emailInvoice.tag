<%--
  User: jonmark
  Date: 3/19/18
  Time: 11:42 AM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.invoices.services.SendInvoiceEmail"/>
${ss:assert(task.invoice.type.nicheAuction, 'This email is only setup to work with Niche Purchase invoices currently.')}

<ss:set var="auctionInvoice" object="${task.invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice" />
<ss:set var="niche" object="${auctionInvoice.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<n_email:emailWrapper
        subject="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailInvoice.subject', niche.nameForHtml)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailInvoice.payInvoice')}"
        actionUrl="${task.invoice.displayUrl}">
    <e:elt name="p">
        <ss:set var="nicheLink">
            <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
        </ss:set>
        <ss:set var="bidValue">
            ${task.invoice.nrveAmount.formattedWithSuffix}
        </ss:set>
        <ss:set var="paymentDueDatetime">
            <comp:datetime datetime="${task.invoice.paymentDueDatetime}" isNoPrettyTime="${true}" />
        </ss:set>
        ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailInvoice.intro', bidValue, nicheLink, paymentDueDatetime)}
    </e:elt>
    <e:elt name="p">
        ${h:wordlet('jsp.site.custom.narrative.email.emailInvoice.warning')}
    </e:elt>
</n_email:emailWrapper>

