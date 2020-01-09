<%@ tag import="org.narrative.network.customizations.narrative.services.ReactRoute" %>
<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%@ tag import="java.util.Arrays" %>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/components" %>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/common/page" %>
<%@ taglib prefix="tool" tagdir="/WEB-INF/tags/common/tool" %>
<%@ taglib prefix="s_page" tagdir="/WEB-INF/tags/site/page" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="narrative" tagdir="/WEB-INF/tags/site/custom/narrative" %>
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="ne_comp" tagdir="/WEB-INF/tags/site/custom/narrative/email/components" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.service.impl.kyc.email.SendKycCertifiedEmailTask"/>

<ss:set var="networkContext" object="${task.networkContext}" className="org.narrative.network.shared.context.NetworkContext"/>
<ss:set var="authZone" object="${networkContext.authZone}" className="org.narrative.network.core.user.AuthZone"/>

<n_email:emailWrapper
        subject="${h:wordlet('tags.site.custom.narrative.email.kyc.certificationAttemptSuccess.subject')}"
        actionText="${h:wordlet('tags.site.custom.narrative.email.kyc.viewCertStatus')}"
        actionUrl="<%= ReactRoute.MEMBER_CERTIFICATION.getUrl()%>">

    <jsp:body>
        <e:elt name="div" cssClass="<%=EmailCssClass.CENTERED_BODY%>">

            <e:elt name="img" cssClasses="<%= Arrays.asList(EmailCssClass.ICON_IMAGE, EmailCssClass.SIGNATURE_WRAPPER)%>"
                   src="${authZone.baseUrl}/email/icon-approve.png"/>

            <e:elt name="p" cssClasses="<%= Arrays.asList(EmailCssClass.TITLE, EmailCssClass.SIGNATURE_WRAPPER) %>">
                ${h:wordlet('tags.site.custom.narrative.email.kyc.certificationAttemptSuccess.subject')}
            </e:elt>

            <e:elt name="p" cssClass="<%=EmailCssClass.EMAIL_CONTENT%>">
                ${h:wordlet('tags.site.custom.narrative.email.kyc.certificationAttemptSuccess.body')}
            </e:elt>

            <c:if test="${task.user.userAgeStatus.over18}">
                <e:elt name="p" cssClass="<%=EmailCssClass.EMAIL_CONTENT%>">
                    <ss:set var="personalSettingsLink">
                        <e:a href="<%=ReactRoute.MEMBER_PERSONAL_SETTINGS.getUrl()%>">
                            ${h:wordlet('tags.site.custom.narrative.email.kyc.certificationAttemptSuccess.preferences')}
                        </e:a>
                    </ss:set>
                    ${h:wordlet1Arg('tags.site.custom.narrative.email.kyc.certificationAttemptSuccess.viewAgeRestrictedPosts', personalSettingsLink)}
                </e:elt>
            </c:if>

        </e:elt>
    </jsp:body>

</n_email:emailWrapper>
