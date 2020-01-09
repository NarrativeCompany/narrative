<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.reputation.services.SendConductNegativeEndedEmailTask"/>

<%-- bl: this has to use a JSTL expression, not a scriptlet, since the task object isn't available in scriptlet scope --%>
<ss:set var="actionUrl" object="${ReactRoute.USER_PROFILE_REPUTATION.getUrl(task.getUser().getIdForUrl())}" />

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.conductNegativeEnded.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.conductNegativeEnded.viewStatus')}"
        actionUrl="${actionUrl}">

    <e:elt name="h1">
        <ss:set var="greenRestored">
            <e:elt name="span" cssClass="<%=EmailCssClass.GREEN_TEXT%>">
                ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeEnded.restored')}
            </e:elt>
        </ss:set>
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.conductNegativeEnded.heading', greenRestored)}
    </e:elt>
    <e:elt name="p">
        ${h:wordlet('jsp.site.custom.narrative.email.conductNegativeEnded.intro')}
    </e:elt>
</n_email:emailWrapper>
