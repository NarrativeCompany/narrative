<%--
  User: jonmark
  Date: 2019-09-26
  Time: 12:19
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
<%@ taglib prefix="n_email" tagdir="/WEB-INF/tags/site/custom/narrative/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.customizations.narrative.publications.services.SendPublicationDeletedEmail"/>

<n_email:emailWrapper subject="${h:wordlet('jsp.site.custom.narrative.email.emailPublicationDeleted.subject')}">
    <e:elt name="p">
        ${h:wordlet1Arg('jsp.site.custom.narrative.email.emailPublicationDeleted.intro', task.publicationName)}
    </e:elt>
</n_email:emailWrapper>
