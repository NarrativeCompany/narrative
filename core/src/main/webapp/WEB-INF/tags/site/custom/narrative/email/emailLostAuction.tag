<%--
  User: jonmark
  Date: 3/19/18
  Time: 11:49 AM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendLostAuctionEmail"/>

<ss:set var="niche" object="${task.bid.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<n_email:emailWrapper
        subject="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailLostAuction.subject', niche.nameForHtml)}"
        actionText="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailLostAuction.viewNiche', niche.nameForHtml)}"
        actionUrl="${niche.displayUrl}">
    <e:elt name="p">
        <ss:set var="nicheLink">
            <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
        </ss:set>
        <ss:set var="winningBidValue">
            ${task.bid.nrveBid.formattedWithSuffix}
        </ss:set>
        <ss:set var="winningBidderLink">
            <e:a href="${task.bid.bidder.user.displayUrl}">${task.bid.bidder.displayNameResolved}</e:a>
        </ss:set>
        <ss:set var="securityDeposit" object="${task.securityDeposit}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit" />

        <ss:set var="wordletSuffix" object="${(ss:exists(securityDeposit) && securityDeposit.invoice.fiatPayment.status.refunded) ? '.withRefund' : ''}" className="java.lang.String" />

        ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailLostAuction.intro', winningBidderLink, nicheLink, winningBidValue)}
    </e:elt>
</n_email:emailWrapper>

