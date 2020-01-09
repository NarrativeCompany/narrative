<%--
  User: jonmark
  Date: 2019-08-06
  Time: 13:16
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

<ss:ref var="task" className="org.narrative.network.customizations.narrative.publications.services.SendPublicationRejectedEmailToOwner"/>

<ss:set var="publication" object="${task.publication}" className="org.narrative.network.customizations.narrative.publications.Publication" />
<ss:set var="dueToIssue" object="${task.dueToIssue}" className="org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue" />

<%-- jw: unlike rejected niches we will not be linking to the publication since it will not be accessible to anyone --%>

<n_email:emailWrapper
        subject="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationRejectedToOwner.subject')}"
        actionText="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationRejectedToOwner.viewAppeal')}"
        actionUrl="${dueToIssue.displayUrl}">
    <e:elt name="p">
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailPublicationRejectedToOwner.intro', publication.nameForHtml)}
    </e:elt>
</n_email:emailWrapper>
