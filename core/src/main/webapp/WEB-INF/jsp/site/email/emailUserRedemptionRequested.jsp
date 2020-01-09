<%@ page import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%--
  User: jonmark
  Date: 2019-07-17
  Time: 14:08
--%>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>

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

<ss:ref var="task" className="org.narrative.network.core.user.services.SendUserRedemptionRequestedEmailTask"/>

<n_email:emailWrapper subject="${h:wordlet('jsp.site.email.emailUserRedemptionRequested.subject')}">
    <jsp:attribute name="cardFooterHtml">
        <ss:set var="transactionsLink">
            <e:a href="${ReactRoute.USER_PROFILE_REWARDS_TRANSACTIONS.getUrl(task.getUser().getIdForUrl())}">
                ${h:wordlet('jsp.site.email.emailUserRedemptionRequested.transactions')}
            </e:a>
        </ss:set>
        <ss:set var="passwordLink">
            <e:a href="${ReactRoute.MEMBER_ACCOUNT_SETTINGS.getUrl()}">
                ${h:wordlet('jsp.site.email.emailUserRedemptionRequested.updateYourPassword')}
            </e:a>
        </ss:set>

        ${h:wordlet2Arg('jsp.site.email.emailUserRedemptionRequested.warning', transactionsLink, passwordLink)}
    </jsp:attribute>
    <jsp:body>
        <ss:set var="walletLink">
            <page:neoscanWalletLink address="${task.transaction.fromWallet.neoWallet.neoAddress}" />
        </ss:set>

        <e:elt name="p">
            ${h:wordlet2Arg('jsp.site.email.emailUserRedemptionRequested.introduction', task.transaction.nrveAmount.formattedWithSuffix, walletLink)}
        </e:elt>

        <e:elt name="p">
            ${h:wordlet('jsp.site.email.emailUserRedemptionRequested.processingDetails')}
        </e:elt>
    </jsp:body>
</n_email:emailWrapper>
