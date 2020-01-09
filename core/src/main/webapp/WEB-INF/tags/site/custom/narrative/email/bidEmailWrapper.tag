<%--
  User: jonmark
  Date: 4/11/18
  Time: 7:19 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="subject" required="true" type="java.lang.String" %>
<%@ attribute name="leadingBid" type="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="ne_comp" tagdir="/WEB-INF/tags/site/custom/narrative/email/components" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendBidEmailBase" />

<ss:set var="auction" object="${task.bid.auction}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction" />

<c:if test="${leadingBid eq null}">
    <ss:set var="leadingBid" object="${auction.leadingBid}" className="org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid" />
</c:if>

<%-- jw: neither of these emails (leading bid, outbid) are controlled by watching the niche, so do not include watchedNiche in the wrapper --%>
<n_email:emailWrapper
        subject="${subject}"
        actionText="${h:wordlet('tags.site.custom.narrative.email.bidEmailWrapper.viewAuction')}"
        actionUrl="${auction.displayUrl}">
    <jsp:attribute name="details">
        <email:detailsTable>
            <ne_comp:nicheDetailTableRow niche="${auction.niche}" />
            <email:detailsTableRow title="${h:wordlet('tags.site.custom.narrative.email.bidEmailWrapper.currentHighBid')}">
                ${leadingBid.nrveBid.formattedWithSuffix}
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('tags.site.custom.narrative.email.bidEmailWrapper.highestBidder')}">
                <s_page:displayName role="${leadingBid.bidder}" />
            </email:detailsTableRow>
            <email:detailsTableRow title="${h:wordlet('tags.site.custom.narrative.email.bidEmailWrapper.auctionEnds')}">
                <comp:datetime datetime="${auction.endDatetime}" isLongFormat="${true}" isNoPrettyTime="${true}" />
            </email:detailsTableRow>
        </email:detailsTable>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody />
    </jsp:body>
</n_email:emailWrapper>
