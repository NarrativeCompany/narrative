<%--
  User: jonmark
  Date: 3/21/18
  Time: 1:33 PM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendPaymentChargebackEmail"/>

<ss:set var="invoice" object="${task.payment.invoice}" className="org.narrative.network.customizations.narrative.invoices.Invoice" />

<ss:set var="wordletSuffix" object=".${invoice.type}" className="java.lang.String" />

<n_email:emailWrapper subject="${h:wordlet('jsp.site.custom.narrative.email.emailPaymentChargeback.subject'+=wordletSuffix)}">
    <e:elt name="p">
        <c:choose>
            <c:when test="${invoice.type.nicheAuction or invoice.type.publicationAnnualFee}">
                <c:choose>
                    <c:when test="${invoice.type.nicheAuction}">
                        <ss:set var="auctionInvoice" object="${invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice" />
                        <ss:set var="channelConsumer" object="${auctionInvoice.auction.niche}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />
                    </c:when>
                    <c:otherwise>
                        ${ss:assert(invoice.type.publicationAnnualFee, 'Expected publication annual fee, not/'+=invoice.type)}
                        <ss:set var="publicationInvoice" object="${invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.publications.PublicationInvoice" />
                        <ss:set var="channelConsumer" object="${publicationInvoice.publication}" className="org.narrative.network.customizations.narrative.channels.ChannelConsumer" />
                    </c:otherwise>
                </c:choose>
                <ss:set var="channelConsumerLink">
                    <e:a href="${channelConsumer.displayUrl}">${channelConsumer.nameForHtml}</e:a>
                </ss:set>
                <ss:set var="paymentValue" object="${task.payment.totalUsdAmountForUi.formattedAsUsd}" className="java.lang.String" />

                ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPaymentChargeback.intro'+=wordletSuffix, paymentValue, channelConsumerLink)}
            </c:when>

            <c:when test="${invoice.type.nicheAuctionSecurityDeposit}">
                <ss:set var="securityDeposit" object="${invoice.invoiceConsumer}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit" />
                <ss:set var="niche" object="${securityDeposit.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />
                <ss:set var="nicheLink">
                    <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
                </ss:set>
                <ss:set var="paymentValue" object="${task.payment.totalUsdAmountForUi.formattedAsUsd}" className="java.lang.String" />

                ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailPaymentChargeback.intro'+=wordletSuffix, paymentValue, nicheLink)}
            </c:when>

            <c:when test="${invoice.type.kycCertification}">
                <ss:set var="paymentValue" object="${task.payment.totalUsdAmountForUi.formattedAsUsd}" className="java.lang.String" />

                ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailPaymentChargeback.intro'+=wordletSuffix, paymentValue)}
            </c:when>

            <c:otherwise>
                ${ss:throwUnexpectedError('Encountered unhandled invoice type/'+=invoice.type)}
            </c:otherwise>
        </c:choose>
    </e:elt>
</n_email:emailWrapper>
