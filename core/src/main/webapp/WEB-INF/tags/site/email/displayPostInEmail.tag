<%@ tag import="org.narrative.network.shared.email.EmailCssClass" %>
<%--
  User: brian
  Date: 10/5/11
  Time: 5:09 PM
--%>
<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>

<%@ attribute name="compositionConsumer" required="true" type="org.narrative.network.core.composition.base.CompositionConsumer" %>
<%@ attribute name="post" required="true" type="org.narrative.network.core.composition.base.PostBase" %>
<%@ attribute name="skipCssClass" required="false" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ss" uri="http://www.narrative.org/tags/core" %>
<%@ taglib prefix="h" uri="http://www.narrative.org/tags/handy" %>
<%@ taglib prefix="email" tagdir="/WEB-INF/tags/site/email" %>
<%@ taglib prefix="e" tagdir="/WEB-INF/tags/common/email/elements" %>

<ss:ref var="task" className="org.narrative.network.shared.tasktypes.NetworkTaskImpl" />

<ss:set var="content" object="${compositionConsumer}" className="org.narrative.network.core.content.base.Content" setNullIfNotOfType="true" />
<ss:set var="reply" object="${post}" className="org.narrative.network.core.composition.base.Reply" setNullIfNotOfType="true" />

<ss:set var="detailsBoxCell" object="<%=EmailCssClass.DETAILS_BOX_CELL%>" className="org.narrative.network.shared.email.EmailCssClass" />

<e:elt name="div" cssClass="${skipCssClass ? null : detailsBoxCell}">
    ${post.bodyForEmail}
</e:elt>
<e:clear />