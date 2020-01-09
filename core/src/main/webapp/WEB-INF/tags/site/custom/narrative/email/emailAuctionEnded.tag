<%--
  User: jonmark
  Date: 3/30/18
  Time: 12:33 PM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendAuctionEndedEmail"/>

<ss:set var="niche" object="${task.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<n_email:emailWrapper
        subject="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailAuctionEnded.subject', niche.nameForHtml)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailAuctionEnded.viewAuction')}"
        actionUrl="${task.auction.displayUrl}"
        watchedChannelConsumer="${niche}">
    <e:elt name="p">
        <ss:set var="nicheLink">
            <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
        </ss:set>
        <ss:set var="winnerLink">
            <s_page:displayName role="${task.auction.leadingBid.bidder}" />
        </ss:set>
        <ss:set var="bidValue">
            ${task.auction.leadingBid.nrveBid.formattedWithSuffix}
        </ss:set>
        ${h:wordlet3Arg('jsp.site.custom.narrative.email.emailAuctionEnded.intro', nicheLink, winnerLink, bidValue)}
    </e:elt>
</n_email:emailWrapper>

