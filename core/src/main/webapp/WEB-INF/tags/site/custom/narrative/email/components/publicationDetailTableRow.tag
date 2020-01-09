<%--
  User: jonmark
  Date: 4/11/18
  Time: 7:21 AM
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>

<%@ attribute name="publication" required="true" type="org.narrative.network.customizations.narrative.publications.Publication" %>

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

<email:detailsTableRow title="${h:wordlet('tags.site.custom.narrative.email.components.publicationDetailTableRow.publication')}">
    <e:a href="${publication.displayUrl}">${publication.nameForHtml}</e:a>
</email:detailsTableRow>
