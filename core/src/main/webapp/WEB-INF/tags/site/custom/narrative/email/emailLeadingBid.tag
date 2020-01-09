<%--
  User: jonmark
  Date: 3/19/18
  Time: 11:32 AM
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.niches.nicheauction.services.SendLeadingBidEmail"/>

<ss:set var="niche" object="${task.bid.auction.niche}" className="org.narrative.network.customizations.narrative.niches.niche.Niche" />

<n_email:bidEmailWrapper subject="${h:wordlet1Arg('tags.site.custom.narrative.email.emailLeadingBid.subject', niche.nameForHtml)}">
    <c:choose>
        <%-- jw: note: due to email preview, we are only going to be checking for null state  --%>
        <c:when test="${task.inResponseToBid ne null and not task.forActiveBidder}">
            <ss:set var="previousBidValue">
                ${task.inResponseToBid.nrveBid.formattedWithSuffix}
            </ss:set>
            <ss:set var="previousBidderLink">
                <s_page:displayName role="${task.inResponseToBid.bidder}" />
            </ss:set>
            ${h:wordlet2Arg('tags.site.custom.narrative.email.emailLeadingBid.intro.forAutomaticBid', previousBidderLink, previousBidValue)}
        </c:when>
        <c:otherwise>
            ${h:wordlet('tags.site.custom.narrative.email.emailLeadingBid.intro')}
        </c:otherwise>
    </c:choose>
</n_email:bidEmailWrapper>
