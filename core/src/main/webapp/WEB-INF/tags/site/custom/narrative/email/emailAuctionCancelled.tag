<%--
  User: jonmark
  Date: 4/4/18
  Time: 11:07 AM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendAuctionCancelledEmail"/>

<ss:set var="niche" object="${task.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<n_email:emailWrapper
        subject="${h:wordlet1Arg('jsp.site.custom.narrative.email.emailAuctionCancelled.subject', niche.nameForHtml)}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailAuctionCancelled.viewAuction')}"
        actionUrl="${task.auction.displayUrl}"
        watchedChannelConsumer="${niche}">
    <e:elt name="p">
        <ss:set var="nicheLink">
            <e:a href="${niche.displayUrl}">${niche.nameForHtml}</e:a>
        </ss:set>
        <ss:set var="auctionLink">
            <e:a href="${task.auction.displayUrl}">${h:wordlet('jsp.site.custom.narrative.email.emailAuctionCancelled.auction')}</e:a>
        </ss:set>
        ${h:wordlet2Arg('jsp.site.custom.narrative.email.emailAuctionCancelled.intro', auctionLink, nicheLink)}
    </e:elt>
</n_email:emailWrapper>

