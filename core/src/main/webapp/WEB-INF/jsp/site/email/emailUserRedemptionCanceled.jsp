<%@ page import="org.narrative.network.customizations.narrative.services.ReactRoute" %><%--
  todo:post-v1.3.2 remove this email JSP once the CancelPendingRedemptionsPatch is deleted
  User: brian
  Date: 2019-08-06
  Time: 18:28
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

<ss:ref var="task" className="org.narrative.network.core.user.services.SendUserRedemptionCanceledEmailTask"/>

<ss:set var="rewardsUrl" object="${ReactRoute.USER_PROFILE_REWARDS.getUrl(task.getUser().getIdForUrl())}" />

<n_email:emailWrapper subject="Narrative Reward Points Redemption Canceled">
    <e:elt name="p">
        There was an issue processing July rewards, so your pending Narrative points Redemption request has been canceled.
        July rewards will be re-processed soon. You can resubmit your Redemption request at any time in the
        <e:a href="${rewardsUrl}">Rewards</e:a> section of your profile.
    </e:elt>

    <e:elt name="p">
        For more details, see the <e:a href="https://www.narrative.org/post/oops-july-rewards-to-be-updated">announcement</e:a>
        on the Narrative blog.
    </e:elt>
</n_email:emailWrapper>
