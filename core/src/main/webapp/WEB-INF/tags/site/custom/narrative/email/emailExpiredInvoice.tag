<%--
  User: jonmark
  Date: 3/25/18
  Time: 9:12 AM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendExpiredInvoiceEmail"/>

<ss:set var="invoice" object="${task.invoice}" className="org.narrative.network.customizations.narrative.invoices.Invoice" />

<ss:set var="wordletSuffix" object=".${invoice.type}" className="java.lang.String" />

<ss:set var="auctionInvoice" object="${task.invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice" />
<ss:set var="niche" object="${auctionInvoice.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<ss:set var="subject">
    <c:choose>
        <c:when test="${invoice.type.nicheAuction}">
            ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailExpiredInvoice.subject'+=wordletSuffix, niche.nameForHtml)}
        </c:when>

        <c:otherwise>
            ${ss:throwUnexpectedError('Encountered unhandled invoice type/'+=invoice.type)}
        </c:otherwise>
    </c:choose>
</ss:set>

<n_email:emailWrapper
        subject="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailExpiredInvoice.subject'+=wordletSuffix, niche.nameForHtml)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailExpiredInvoice.viewInvoice')}"
        actionUrl="${task.invoice.displayUrl}">
    <e:elt name="p">
        <c:choose>
            <c:when test="${invoice.type.nicheAuction}">
                <ss:set var="nicheLink">
                    <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
                </ss:set>
                <ss:set var="bidValue">
                    ${task.invoice.nrveAmount.formattedWithSuffix}
                </ss:set>
                <ss:set var="paymentDueDatetime">
                    <comp:datetime datetime="${task.invoice.paymentDueDatetime}" isNoPrettyTime="${true}" />
                </ss:set>
                <ss:set var="securityDeposit" object="${task.securityDeposit}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit" />
                <ss:set var="extraSuffix" object="${(ss:exists(securityDeposit) && securityDeposit.invoice.fiatPayment.status.paid) ? '.withDeposit' : ''}" className="java.lang.String" />

                ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailExpiredInvoice.intro'+=wordletSuffix+=extraSuffix, bidValue, nicheLink, paymentDueDatetime)}
            </c:when>

            <c:otherwise>
                ${ss:throwUnexpectedError('Encountered unhandled invoice type/'+=invoice.type)}
            </c:otherwise>
        </c:choose>
    </e:elt>
</n_email:emailWrapper>
